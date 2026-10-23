package com.taihoang.socialmobile

/**
 * Keeps connection-level errors (for example, a Redis failure during the
 * presence heartbeat) out of an unrelated chat screen.
 */
internal fun isChatErrorRelevant(
    errorConversationId: Long?,
    clientMessageId: String?,
    conversationId: Long,
    messages: List<ApiMessage>
): Boolean {
    if (errorConversationId == conversationId) return true

    clientMessageId ?: return false
    return messages.any {
        it.conversationId == conversationId && it.clientMessageId == clientMessageId
    }
}

/** Payload of /user/queue/conversations.events (backend GroupRealtimeEvent). */
data class GroupConversationEvent(
    val type: String,
    val conversationId: Long,
    val actorUserId: Long?,
    val targetUserIds: List<Long>,
    val name: String?,
    val avatarUrl: String?
)

internal data class GroupEventResult(
    val conversations: List<ApiConversation>,
    /** The current user was added to a group that is not in the loaded list yet. */
    val reloadConversations: Boolean
)

private val MEMBER_REMOVAL_TYPES = setOf("GROUP_MEMBER_REMOVED", "GROUP_MEMBER_LEFT")

internal fun GroupConversationEvent.removesUser(userId: Long?): Boolean =
    userId != null && type in MEMBER_REMOVAL_TYPES && userId in targetUserIds

internal fun applyGroupEvent(
    conversations: List<ApiConversation>,
    event: GroupConversationEvent,
    currentUserId: Long?
): GroupEventResult {
    fun update(transform: (ApiConversation) -> ApiConversation) =
        GroupEventResult(conversations.map { if (it.id == event.conversationId) transform(it) else it }, false)

    return when {
        event.removesUser(currentUserId) ->
            GroupEventResult(conversations.filterNot { it.id == event.conversationId }, false)
        event.type == "GROUP_NAME_UPDATED" && !event.name.isNullOrBlank() ->
            update { it.copy(name = event.name) }
        event.type == "GROUP_AVATAR_UPDATED" ->
            update { it.copy(avatarUrl = event.avatarUrl) }
        event.type == "GROUP_MEMBERS_ADDED" ->
            GroupEventResult(
                conversations,
                reloadConversations = currentUserId != null && currentUserId in event.targetUserIds &&
                    conversations.none { it.id == event.conversationId }
            )
        else -> GroupEventResult(conversations, false)
    }
}

/** Payload of /user/queue/story.reactions, delivered to the story author. */
data class StoryReactionEvent(
    val storyId: Long,
    val actorId: Long?,
    val actorName: String?,
    val type: String?,
    val action: String,
    val nonce: Long = System.nanoTime()
)

/** ADD adds a reaction, REMOVE drops one, UPDATE only changes its type. */
internal fun storyReactionDelta(action: String): Int = when (action.uppercase()) {
    "ADD" -> 1
    "REMOVE" -> -1
    else -> 0
}

internal fun ApiStory.withReactionEvents(events: List<StoryReactionEvent>): ApiStory {
    val delta = events.filter { it.storyId == id }.sumOf { storyReactionDelta(it.action) }
    return if (delta == 0) this else copy(reactionCount = (reactionCount + delta).coerceAtLeast(0))
}

/** Events newer than [afterNonce], so each one is applied exactly once per consumer. */
internal fun List<StoryReactionEvent>.newerThan(afterNonce: Long): List<StoryReactionEvent> =
    filter { it.nonce > afterNonce }
