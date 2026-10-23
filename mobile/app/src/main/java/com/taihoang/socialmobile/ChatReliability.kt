package com.taihoang.socialmobile

internal const val CHAT_SEND_ACK_TIMEOUT_MS = 10_000L
internal const val CHAT_TYPING_TIMEOUT_MS = 6_000L
/** Must stay below [CHAT_TYPING_TIMEOUT_MS] so receivers keep showing the indicator while typing continues. */
internal const val CHAT_TYPING_RESEND_MS = 3_000L
internal const val CHAT_TYPING_IDLE_MS = 3_000L

/**
 * Turns keystrokes into at most one typing=true frame per [resendMs] and a single typing=false
 * when the user stops, instead of one STOMP frame (and one server-side member lookup) per keystroke.
 */
internal class TypingThrottle(private val resendMs: Long = CHAT_TYPING_RESEND_MS) {
    private var startedAt: Long? = null

    /** True when this keystroke at [now] should send typing=true. */
    fun onInput(now: Long): Boolean {
        val last = startedAt
        if (last != null && now >= last && now - last < resendMs) return false
        startedAt = now
        return true
    }

    /** True when typing=false should be sent: once, and only if typing=true was sent before. */
    fun onStop(): Boolean {
        if (startedAt == null) return false
        startedAt = null
        return true
    }
}

/**
 * Reconciles optimistic messages with server acknowledgements without duplicates.
 * The server response may have a new id while preserving the clientMessageId.
 */
internal fun mergeChatMessages(
    current: List<ApiMessage>,
    incoming: List<ApiMessage>
): List<ApiMessage> {
    val merged = ArrayList<ApiMessage>(current.size + incoming.size)
    // First position of each id / clientMessageId in `merged`, so a candidate is matched in O(1)
    // instead of scanning the whole list (O(n²) once the history gets long).
    val firstById = HashMap<Long, Int>()
    val firstByClientId = HashMap<String, Int>()

    fun index(position: Int) {
        val message = merged[position]
        message.id?.let { if ((firstById[it] ?: Int.MAX_VALUE) > position) firstById[it] = position }
        message.clientMessageId?.let { if ((firstByClientId[it] ?: Int.MAX_VALUE) > position) firstByClientId[it] = position }
    }

    // Rare: a merge dropped a key the position was the first holder of, so find the next holder.
    fun <K> unindex(first: HashMap<K, Int>, key: K?, position: Int, keyOf: (ApiMessage) -> K?) {
        if (key == null || first[key] != position || keyOf(merged[position]) == key) return
        val next = merged.indexOfFirst { keyOf(it) == key }
        if (next >= 0) first[key] = next else first.remove(key)
    }

    for (candidate in current + incoming) {
        val existingIndex = minOf(
            candidate.id?.let { firstById[it] } ?: Int.MAX_VALUE,
            candidate.clientMessageId?.let { firstByClientId[it] } ?: Int.MAX_VALUE
        )
        if (existingIndex == Int.MAX_VALUE) {
            merged += candidate
            index(merged.lastIndex)
        } else {
            val previous = merged[existingIndex]
            merged[existingIndex] = mergeChatMessageState(previous, candidate)
            unindex(firstById, previous.id, existingIndex) { it.id }
            unindex(firstByClientId, previous.clientMessageId, existingIndex) { it.clientMessageId }
            index(existingIndex)
        }
    }
    return merged.sortedWith(
        compareBy<ApiMessage> { it.sentAt.orEmpty() }
            .thenBy { it.sequenceNumber ?: Long.MAX_VALUE }
    )
}

internal fun shouldScrollToLatestMessageAfterOpening(
    loading: Boolean,
    messageCount: Int,
    initialScrollCompleted: Boolean
): Boolean = !loading && messageCount > 0 && !initialScrollCompleted

/** Keeps state that can only move forward when a sparse duplicate payload arrives. */
internal fun mergeChatMessageState(existing: ApiMessage, incoming: ApiMessage): ApiMessage = incoming.copy(
    sentAt = incoming.sentAt ?: existing.sentAt,
    editedAt = incoming.editedAt ?: existing.editedAt,
    recalledAt = incoming.recalledAt ?: existing.recalledAt,
    deliveredAt = incoming.deliveredAt ?: existing.deliveredAt,
    seenAt = incoming.seenAt ?: existing.seenAt,
    sequenceNumber = incoming.sequenceNumber ?: existing.sequenceNumber,
    reactions = if (incoming.reactions.isEmpty()) existing.reactions else incoming.reactions,
    attachments = if (incoming.attachments.isEmpty() && existing.attachments.isNotEmpty()) existing.attachments else incoming.attachments,
    localStatus = if (incoming.id != null) incoming.localStatus else incoming.localStatus ?: existing.localStatus,
    localUploadIds = if (incoming.localUploadIds.isEmpty()) existing.localUploadIds else incoming.localUploadIds,
    localSendAttempt = if (incoming.localSendAttempt == 0L) existing.localSendAttempt else incoming.localSendAttempt,
    callType = incoming.callType ?: existing.callType,
    callStatus = incoming.callStatus ?: existing.callStatus,
    callDurationSeconds = incoming.callDurationSeconds ?: existing.callDurationSeconds,
    callOutgoing = incoming.callOutgoing ?: existing.callOutgoing
)

/** Merges a refreshed conversation page without regressing newer local previews or read state. */
internal fun mergeConversationSnapshots(
    current: List<ApiConversation>,
    refreshed: List<ApiConversation>,
    activeConversationId: Long? = null
): List<ApiConversation> {
    val currentById = current.associateBy { it.id }
    val refreshedIds = refreshed.mapTo(mutableSetOf()) { it.id }
    val merged = refreshed.map { incoming ->
        val existing = currentById[incoming.id]
        if (existing == null) {
            if (incoming.id == activeConversationId) incoming.copy(unreadCount = 0) else incoming
        } else {
            val incomingIsNewer = conversationSnapshotIsAtLeastAsNew(incoming, existing)
            val base = if (incomingIsNewer) incoming else existing
            base.copy(
                unreadCount = when {
                    incoming.id == activeConversationId -> 0
                    !incomingIsNewer -> existing.unreadCount
                    incoming.lastMessageId == existing.lastMessageId -> existing.unreadCount
                    else -> incoming.unreadCount
                }
            )
        }
    }
    val localOnly = current.filter { it.id !in refreshedIds }
    return (merged + localOnly).sortedWith(
        compareByDescending<ApiConversation> { it.updatedAt.orEmpty() }
            .thenBy { it.id }
    )
}

private fun conversationSnapshotIsAtLeastAsNew(
    incoming: ApiConversation,
    existing: ApiConversation
): Boolean {
    if (incoming.lastMessageId != null && existing.lastMessageId != null) {
        return incoming.lastMessageId >= existing.lastMessageId
    }
    if (incoming.updatedAt != null && existing.updatedAt != null) {
        return incoming.updatedAt >= existing.updatedAt
    }
    return incoming.lastMessageId != null || existing.lastMessageId == null
}

internal fun canRetryChatMessage(message: ApiMessage): Boolean =
    message.id == null && message.localStatus == "FAILED" && !message.clientMessageId.isNullOrBlank()

internal fun isTypingFresh(lastTypingAt: Long, now: Long, timeoutMs: Long = CHAT_TYPING_TIMEOUT_MS): Boolean =
    now >= lastTypingAt && now - lastTypingAt < timeoutMs

/** Updates an existing conversation preview; a missing conversation must be reloaded from the API. */
internal fun updateConversationAfterMessage(
    conversations: List<ApiConversation>,
    message: ApiMessage,
    currentUserId: Long?,
    activeConversationId: Long? = null
): List<ApiConversation>? {
    val conversationId = message.conversationId ?: return null
    val current = conversations.firstOrNull { it.id == conversationId } ?: return null
    val preview = message.content.ifBlank {
        if (message.attachments.isNotEmpty()) "Tep dinh kem" else current.lastMessage
    }
    val updated = current.copy(
        lastMessage = preview,
        lastMessageId = message.id ?: current.lastMessageId,
        lastMessageSenderName = message.senderName,
        updatedAt = message.sentAt,
        unreadCount = if (message.senderId != currentUserId && conversationId != activeConversationId) {
            current.unreadCount + 1
        } else if (conversationId == activeConversationId) {
            0
        } else {
            current.unreadCount
        }
    )
    return listOf(updated) + conversations.filterNot { it.id == conversationId }
}

/** Applies the local read transition immediately; the REST/STOMP acknowledgement follows. */
internal fun markMessagesSeenLocally(
    messages: List<ApiMessage>,
    conversationId: Long,
    currentUserId: Long?,
    seenAt: String
): List<ApiMessage> = messages.map { message ->
    if (
        message.conversationId == conversationId &&
        message.senderId != null &&
        message.senderId != currentUserId &&
        message.seenAt == null
    ) message.copy(seenAt = seenAt) else message
}

/** Makes an accepted friend searchable immediately while the authoritative refresh is in flight. */
internal fun mergeAcceptedFriend(current: List<ApiFriend>, accepted: ApiFriend): List<ApiFriend> =
    (listOf(accepted) + current.filterNot { it.id == accepted.id })
        .sortedBy { it.name.lowercase() }

/** Inserts or replaces a conversation immediately after the create-direct response. */
internal fun upsertConversation(
    current: List<ApiConversation>,
    conversation: ApiConversation
): List<ApiConversation> =
    listOf(conversation) + current.filterNot { it.id == conversation.id }

/** Reconciles the latest page without dropping older paginated history. */
internal fun replaceLatestMessagePage(
    current: List<ApiMessage>,
    refreshed: List<ApiMessage>,
    latestPageSize: Int? = null
): List<ApiMessage> {
    val persisted = current.filter { it.id != null }
    val replacedIds = persisted
        .takeLast((latestPageSize ?: persisted.size).coerceAtLeast(0))
        .mapNotNull { it.id }
        .toSet()
    val preserved = current.filter { it.id == null || it.id !in replacedIds }
    return mergeChatMessages(preserved, refreshed)
}

/** Returns persisted messages received from another user and safe to acknowledge. */
internal fun incomingMessageIds(messages: List<ApiMessage>, currentUserId: Long?): List<Long> {
    if (currentUserId == null) return emptyList()
    return messages.asSequence()
        .filter { it.senderId != null && it.senderId != currentUserId && it.seenAt == null }
        .mapNotNull { it.id }
        .distinct()
        .toList()
}
