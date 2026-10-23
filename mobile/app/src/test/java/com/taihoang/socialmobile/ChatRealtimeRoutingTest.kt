package com.taihoang.socialmobile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatRealtimeRoutingTest {
    private val conversationId = 42L

    @Test
    fun presenceHeartbeatErrorIsNotShownInChat() {
        assertFalse(isChatErrorRelevant(null, null, conversationId, emptyList()))
    }

    @Test
    fun errorWithConversationIdIsShownInThatChat() {
        assertTrue(isChatErrorRelevant(conversationId, null, conversationId, emptyList()))
    }

    @Test
    fun sendErrorUsesMatchingClientMessageId() {
        val message = ApiMessage(
            id = null,
            conversationId = conversationId,
            senderId = 7L,
            senderName = "Me",
            content = "hello",
            messageType = "USER",
            sentAt = null,
            editedAt = null,
            recalledAt = null,
            seenAt = null,
            clientMessageId = "client-1"
        )

        assertTrue(isChatErrorRelevant(null, "client-1", conversationId, listOf(message)))
        assertFalse(isChatErrorRelevant(null, "client-1", 99L, listOf(message)))
    }
}
