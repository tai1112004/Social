package com.taihoang.socialmobile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatReliabilityTest {
    @Test
    fun serverAckReplacesOptimisticMessageByClientMessageId() {
        val optimistic = message(id = null, clientMessageId = "client-1", content = "hello")
        val acknowledged = message(id = 42L, clientMessageId = "client-1", content = "hello")

        val result = mergeChatMessages(listOf(optimistic), listOf(acknowledged))

        assertEquals(1, result.size)
        assertEquals(42L, result.single().id)
    }

    @Test
    fun sparseAckDoesNotRegressDeliveryStateOrRemoveExistingReactions() {
        val deliveredAndReacted = message(
            id = 42L,
            clientMessageId = "client-1",
            content = "hello",
            deliveredAt = "2026-10-12T00:01:00",
            seenAt = "2026-10-12T00:02:00",
            reactions = listOf(ApiMessageReaction(8L, "Other User", "LOVE"))
        )
        val sparseAck = message(id = 42L, clientMessageId = "client-1", content = "hello")

        val result = mergeChatMessages(listOf(deliveredAndReacted), listOf(sparseAck)).single()

        assertEquals("2026-10-12T00:01:00", result.deliveredAt)
        assertEquals("2026-10-12T00:02:00", result.seenAt)
        assertEquals(listOf("LOVE"), result.reactions.map { it.type })
    }

    @Test
    fun onlyUnacknowledgedFailedMessagesCanBeRetried() {
        val failed = message(id = null, clientMessageId = "client-failed", content = "retry", localStatus = "FAILED")
        val acknowledged = message(id = 42L, clientMessageId = "client-ack", content = "sent", localStatus = "FAILED")

        assertTrue(canRetryChatMessage(failed))
        assertTrue(!canRetryChatMessage(acknowledged))
    }

    @Test
    fun typingStateExpiresAfterTheConfiguredWindow() {
        assertTrue(isTypingFresh(9_000L, now = 14_000L, timeoutMs = 6_000L))
        assertTrue(!isTypingFresh(8_000L, now = 14_001L, timeoutMs = 6_000L))
    }

    @Test
    fun duplicateHistoryPagesRemainOrderedAndKeepTheRicherMessage() {
        val current = message(
            id = 3L,
            clientMessageId = "client-3",
            content = "current",
            sequenceNumber = 3L,
            deliveredAt = "2026-10-12T00:01:00"
        )
        val overlappingOlder = message(
            id = 3L,
            clientMessageId = "client-3",
            content = "stale overlap",
            sequenceNumber = 3L
        )
        val older = message(id = 2L, clientMessageId = "client-2", content = "older", sequenceNumber = 2L)

        val result = mergeChatMessages(listOf(older, overlappingOlder), listOf(current))

        assertEquals(listOf(2L, 3L), result.mapNotNull { it.id })
        assertEquals("current", result.last().content)
        assertEquals("2026-10-12T00:01:00", result.last().deliveredAt)
    }

    @Test
    fun differentMessagesRemainInSequenceOrder() {
        val first = message(id = 1L, clientMessageId = "client-1", content = "first", sequenceNumber = 1L)
        val second = message(id = 2L, clientMessageId = "client-2", content = "second", sequenceNumber = 2L)

        val result = mergeChatMessages(listOf(second), listOf(first))

        assertEquals(listOf(1L, 2L), result.mapNotNull { it.id })
        assertTrue(result.all { it.clientMessageId != null })
    }

    @Test
    fun callHistoryIsPlacedBetweenMessagesByItsTimestamp() {
        val beforeCall = message(id = 1L, clientMessageId = "message-before", content = "before", sequenceNumber = 10L)
            .copy(sentAt = "2026-10-23T10:00:00")
        val call = message(id = null, clientMessageId = "call:call-1", content = "call", sequenceNumber = null)
            .copy(messageType = "SYSTEM", sentAt = "2026-10-23T10:01:00")
        val afterCall = message(id = 2L, clientMessageId = "message-after", content = "after", sequenceNumber = 11L)
            .copy(sentAt = "2026-10-23T10:02:00")

        val result = mergeChatMessages(listOf(beforeCall, afterCall), listOf(call))

        assertEquals(
            listOf("message-before", "call:call-1", "message-after"),
            result.map { it.clientMessageId }
        )
    }

    @Test
    fun newlyOpenedChatScrollsOnceAfterItsMessagesHaveLoaded() {
        assertTrue(shouldScrollToLatestMessageAfterOpening(loading = false, messageCount = 1, initialScrollCompleted = false))
        assertFalse(shouldScrollToLatestMessageAfterOpening(loading = true, messageCount = 1, initialScrollCompleted = false))
        assertFalse(shouldScrollToLatestMessageAfterOpening(loading = false, messageCount = 0, initialScrollCompleted = false))
        assertFalse(shouldScrollToLatestMessageAfterOpening(loading = false, messageCount = 1, initialScrollCompleted = true))
    }

    @Test
    fun deliveredAcknowledgementsOnlyIncludeReceivedMessages() {
        val ownMessage = message(id = 1L, clientMessageId = "client-1", content = "mine", senderId = 7L)
        val receivedMessage = message(id = 2L, clientMessageId = "client-2", content = "theirs", senderId = 8L)
        val pendingReceivedMessage = message(id = null, clientMessageId = "client-3", content = "pending", senderId = 8L)

        val ids = incomingMessageIds(listOf(ownMessage, receivedMessage, pendingReceivedMessage), 7L)

        assertEquals(listOf(2L), ids)
    }

    @Test
    fun seenMessagesAreNotAcknowledgedAsNewIncomingMessagesAgain() {
        val alreadySeen = message(
            id = 1L,
            clientMessageId = "client-1",
            content = "already seen",
            senderId = 8L,
            seenAt = "2026-10-12T00:02:00"
        )
        val unseen = message(id = 2L, clientMessageId = "client-2", content = "unseen", senderId = 8L)

        assertEquals(listOf(2L), incomingMessageIds(listOf(alreadySeen, unseen), 7L))
    }

    @Test
    fun receivedMessagesAreEnoughToMarkTheConversationSeen() {
        val receivedMessage = message(id = 2L, clientMessageId = "client-2", content = "theirs", senderId = 8L)

        assertTrue(incomingMessageIds(listOf(receivedMessage), 7L).isNotEmpty())
    }

    @Test
    fun incomingMessageUpdatesExistingConversationPreviewAndMovesItToTheTop() {
        val first = ApiConversation(1L, "First", null, "old", 0, updatedAt = "2026-10-12T00:00:00")
        val second = ApiConversation(2L, "Second", null, "older", 0, updatedAt = "2026-10-11T00:00:00")
        val incoming = message(id = 9L, clientMessageId = "client-9", content = "new", senderId = 8L).copy(
            conversationId = 2L,
            sentAt = "2026-10-12T00:03:00"
        )

        val result = updateConversationAfterMessage(listOf(first, second), incoming, 7L)!!

        assertEquals(listOf(2L, 1L), result.map { it.id })
        assertEquals("new", result.first().lastMessage)
        assertEquals(1, result.first().unreadCount)
    }

    @Test
    fun incomingMessageDoesNotIncreaseUnreadForTheOpenConversation() {
        val conversation = ApiConversation(2L, "Second", null, "older", 2, updatedAt = "2026-10-12T00:00:00")
        val incoming = message(id = 9L, clientMessageId = "client-9", content = "new", senderId = 8L).copy(
            conversationId = 2L,
            sentAt = "2026-10-12T00:03:00"
        )

        val result = updateConversationAfterMessage(listOf(conversation), incoming, 7L, activeConversationId = 2L)!!

        assertEquals(0, result.single().unreadCount)
    }

    @Test
    fun openConversationMarksReceivedMessagesSeenLocally() {
        val received = message(id = 2L, clientMessageId = "client-2", content = "theirs", senderId = 8L)
        val own = message(id = 3L, clientMessageId = "client-3", content = "mine", senderId = 7L)

        val result = markMessagesSeenLocally(listOf(received, own), 7L, 7L, "2026-10-12T00:02:00")

        assertEquals("2026-10-12T00:02:00", result[0].seenAt)
        assertEquals(null, result[1].seenAt)
    }

    @Test
    fun acceptedFriendIsAvailableBeforeRefreshCompletes() {
        val existing = ApiFriend(1L, "Alice", null, null)
        val accepted = ApiFriend(2L, "Bob", null, null)

        val result = mergeAcceptedFriend(listOf(existing), accepted)

        assertEquals(listOf(1L, 2L), result.map { it.id })
    }

    @Test
    fun newlyCreatedConversationIsImmediatelyVisibleBeforeRefreshCompletes() {
        val existing = ApiConversation(1L, "Existing", null, "old", 0)
        val created = ApiConversation(2L, "Bob", null, "", 0)

        val result = upsertConversation(listOf(existing), created)

        assertEquals(listOf(2L, 1L), result.map { it.id })
    }

    @Test
    fun acceptedFriendIsStillAvailableAfterRefreshingTheOldSnapshot() {
        val result = mergeAcceptedFriend(
            listOf(ApiFriend(1L, "Alice", null, null)),
            ApiFriend(2L, "Bob", null, null)
        )

        assertTrue(result.any { it.name == "Bob" })
    }

    @Test
    fun refreshedLatestPageReplacesStaleMessagesButKeepsOlderHistory() {
        val older = message(id = 1L, clientMessageId = "client-1", content = "older", sequenceNumber = 1L)
        val staleLatest = message(id = 2L, clientMessageId = "client-2", content = "deleted", sequenceNumber = 2L)
        val latest = message(id = 3L, clientMessageId = "client-3", content = "latest", sequenceNumber = 3L)
        val refreshed = message(id = 4L, clientMessageId = "client-4", content = "new latest", sequenceNumber = 4L)
        val optimistic = message(id = null, clientMessageId = "client-pending", content = "sending", sequenceNumber = null)

        val result = replaceLatestMessagePage(
            current = listOf(older, staleLatest, latest, optimistic),
            refreshed = listOf(latest, refreshed),
            latestPageSize = 2
        )

        assertEquals(listOf(1L, 3L, 4L), result.mapNotNull { it.id })
        assertTrue(result.any { it.clientMessageId == "client-pending" })
        assertTrue(result.none { it.id == staleLatest.id })
    }

    @Test
    fun staleConversationRefreshDoesNotRestoreUnreadMessagesAfterLocalRead() {
        val current = ApiConversation(
            id = 7L,
            name = "Bob",
            avatarUrl = null,
            lastMessage = "latest",
            unreadCount = 0,
            updatedAt = "2026-10-12T00:03:00",
            lastMessageId = 9L
        )
        val stale = current.copy(unreadCount = 3, updatedAt = "2026-10-12T00:02:00", lastMessageId = 8L)

        val result = mergeConversationSnapshots(listOf(current), listOf(stale))

        assertEquals(0, result.single().unreadCount)
        assertEquals(9L, result.single().lastMessageId)
    }

    @Test
    fun refreshedConversationWithNewMessageCanIncreaseUnreadCount() {
        val current = ApiConversation(7L, "Bob", null, "old", 0, updatedAt = "2026-10-12T00:02:00", lastMessageId = 8L)
        val refreshed = ApiConversation(7L, "Bob", null, "new", 1, updatedAt = "2026-10-12T00:03:00", lastMessageId = 9L)

        val result = mergeConversationSnapshots(listOf(current), listOf(refreshed))

        assertEquals("new", result.single().lastMessage)
        assertEquals(1, result.single().unreadCount)
    }

    @Test
    fun indexedMergeGivesExactlyTheSameResultAsTheOldLinearScan() {
        // Tiny id/clientMessageId pools force collisions, sparse payloads and keys that change on merge.
        val random = java.util.Random(42)
        repeat(2_000) {
            val current = List(random.nextInt(10)) { randomMessage(random) }
            val incoming = List(random.nextInt(10)) { randomMessage(random) }

            assertEquals(linearMerge(current, incoming), mergeChatMessages(current, incoming))
        }
    }

    @Test
    fun mergingIntoALongHistoryKeepsEveryMessageOnce() {
        val history = (1L..20_000L).map { message(id = it, clientMessageId = "c-$it", content = "m$it", sequenceNumber = it) }
        val ack = message(id = 20_001L, clientMessageId = "c-20001", content = "new", sequenceNumber = 20_001L)

        val result = mergeChatMessages(history, listOf(ack, history[5]))

        assertEquals(20_001, result.size)
        assertEquals((1L..20_001L).toList(), result.map { it.sequenceNumber })
    }

    @Test
    fun continuousTypingSendsOneStartPerResendWindowNotOnePerKeystroke() {
        val throttle = TypingThrottle()
        // 50 keystrokes, one every 200 ms.
        val starts = (0 until 50).count { throttle.onInput(it * 200L) }

        assertEquals(4, starts) // at 0, 3000, 6000 and 9000 ms
        assertTrue(throttle.onStop())
        assertFalse(throttle.onStop())
    }

    @Test
    fun typingStopIsOnlySentAfterAStartAndTypingAgainRestartsImmediately() {
        val throttle = TypingThrottle()

        assertFalse(throttle.onStop())
        assertTrue(throttle.onInput(0L))
        assertTrue(throttle.onStop())
        assertTrue(throttle.onInput(100L))
    }

    @Test
    fun typingIsResentBeforeTheReceiverDropsIt() {
        assertTrue(CHAT_TYPING_RESEND_MS < CHAT_TYPING_TIMEOUT_MS)
    }

    /** The previous O(n²) implementation, kept as the reference for the indexed one. */
    private fun linearMerge(current: List<ApiMessage>, incoming: List<ApiMessage>): List<ApiMessage> {
        val merged = mutableListOf<ApiMessage>()
        (current + incoming).forEach { candidate ->
            val existingIndex = merged.indexOfFirst { existing ->
                (candidate.id != null && existing.id == candidate.id) ||
                    (candidate.clientMessageId != null && existing.clientMessageId == candidate.clientMessageId)
            }
            if (existingIndex >= 0) {
                merged[existingIndex] = mergeChatMessageState(merged[existingIndex], candidate)
            } else {
                merged += candidate
            }
        }
        return merged.sortedWith(
            compareBy<ApiMessage> { it.sentAt.orEmpty() }
                .thenBy { it.sequenceNumber ?: Long.MAX_VALUE }
        )
    }

    private fun randomMessage(random: java.util.Random): ApiMessage {
        fun <T> pick(vararg values: T): T = values[random.nextInt(values.size)]
        return ApiMessage(
            id = pick(null, 1L, 2L, 3L),
            conversationId = 7L,
            senderId = pick(3L, 4L),
            senderName = "Test User",
            content = "m${random.nextInt(1_000)}",
            messageType = "TEXT",
            sentAt = pick(null, "2026-10-12T00:00:00", "2026-10-12T00:01:00"),
            editedAt = pick(null, "2026-10-12T00:05:00"),
            recalledAt = null,
            seenAt = pick(null, "2026-10-12T00:02:00"),
            clientMessageId = pick(null, "a", "b", "c"),
            sequenceNumber = pick(null, 1L, 2L, 3L),
            reactions = pick(emptyList(), listOf(ApiMessageReaction(8L, "Other User", "LOVE"))),
            deliveredAt = pick(null, "2026-10-12T00:01:30"),
            localStatus = pick(null, "PENDING", "FAILED"),
            localSendAttempt = pick(0L, 5L)
        )
    }

    private fun message(
        id: Long?,
        clientMessageId: String,
        content: String,
        sequenceNumber: Long? = null,
        senderId: Long = 3L,
        deliveredAt: String? = null,
        seenAt: String? = null,
        reactions: List<ApiMessageReaction> = emptyList(),
        localStatus: String? = null
    ) = ApiMessage(
        id = id,
        conversationId = 7L,
        senderId = senderId,
        senderName = "Test User",
        content = content,
        messageType = "TEXT",
        sentAt = "2026-10-12T00:00:00",
        editedAt = null,
        recalledAt = null,
        seenAt = seenAt,
        clientMessageId = clientMessageId,
        sequenceNumber = sequenceNumber,
        reactions = reactions,
        deliveredAt = deliveredAt,
        localStatus = localStatus
    )
}
