package com.taihoang.socialmobile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CallModelsTest {
    @Test
    fun localIceCandidatesAreBufferedUntilTheCallIdExists() {
        val buffer = LocalIceCandidateBuffer()
        val candidate = CallIceCandidate("0", 0, "candidate:1")

        buffer.add(candidate)

        assertEquals(listOf(candidate), buffer.drain())
        assertTrue(buffer.drain().isEmpty())
    }

    @Test
    fun stableSignalingCallIdWinsWhileComposeStateCatchesUp() {
        assertEquals("call-new", resolveSignalingCallId("call-new", ""))
        assertEquals("call-active", resolveSignalingCallId(null, "call-active"))
        assertEquals(null, resolveSignalingCallId("", ""))
    }

    @Test
    fun onlyConnectingPhasesUseConnectionTimeout() {
        assertTrue(shouldTimeoutCallPhase("CONNECTING"))
        assertTrue(shouldTimeoutCallPhase("RECONNECTING"))
        assertFalse(shouldTimeoutCallPhase("ACTIVE"))
        assertFalse(shouldTimeoutCallPhase("INCOMING"))
    }

    @Test
    fun acceptIsDroppedWhenTheCallEndedDuringPrepare() {
        assertTrue(canAcceptAfterPrepare("call-23", activeCallId = "call-23", phase = "INCOMING"))
        // Caller cancelled at the same moment (call 23 on 22/10): finishCall already cleared it.
        assertFalse(canAcceptAfterPrepare("call-23", activeCallId = null, phase = "IDLE"))
        // A different call took its place while preparing.
        assertFalse(canAcceptAfterPrepare("call-23", activeCallId = "call-24", phase = "INCOMING"))
    }

    @Test
    fun staleTerminalEventDoesNotApplyToTheActiveCall() {
        assertFalse(shouldApplyCallEvent("call-current", "call-old"))
        assertTrue(shouldApplyCallEvent("call-current", "call-current"))
    }

    @Test
    fun terminalCallEventBecomesOneSystemChatMessage() {
        val message = callEvent(type = "CALL_END", callId = "call-42").toTerminalChatMessage("2026-10-21T12:00:00")

        assertNotNull(message)
        assertEquals("Cuoc goi audio: Cuoc goi da ket thuc", message!!.content)
        assertEquals("SYSTEM", message.messageType)
        assertEquals("call:call-42", message.clientMessageId)
        assertEquals(null, message.senderId)
    }

    @Test
    fun signalingEventDoesNotCreateChatMessage() {
        assertEquals(null, callEvent(type = "CALL_ACCEPT").toTerminalChatMessage("now"))
    }

    @Test
    fun persistedCallHistoryBecomesTheSameStableChatMessage() {
        val history = ApiCallHistory(
            id = 12L,
            callId = "call-42",
            conversationId = 99L,
            otherUserId = 8L,
            otherUserName = "Callee",
            otherUserAvatarUrl = null,
            callType = "AUDIO",
            status = "ENDED",
            createdAt = "2026-10-21T11:59:00",
            answeredAt = "2026-10-21T12:00:00",
            endedAt = "2026-10-21T12:02:00",
            durationSeconds = 120L,
            outgoing = true
        )

        val message = history.toChatMessage()

        assertEquals(99L, message.conversationId)
        assertEquals("call:call-42", message.clientMessageId)
        assertEquals("2026-10-21T12:02:00", message.sentAt)
        assertEquals("Cuoc goi audio: Cuoc goi da ket thuc (120s)", message.content)
        assertEquals("AUDIO", message.callType)
        assertEquals("ENDED", message.callStatus)
        assertEquals(120L, message.callDurationSeconds)
        assertTrue(message.callOutgoing == true)
        assertEquals("2 phut", message.callChatPresentation()?.detail)
    }

    @Test
    fun busyCalleeIsShownInChatAndHistory() {
        assertEquals(
            "Cuoc goi video: Nguoi nhan dang ban",
            callEvent(type = "CALL_BUSY").copy(callType = "VIDEO").toTerminalChatMessage("now")?.content
        )
        val history = ApiCallHistory(
            id = 13L, callId = "call-43", conversationId = 99L, otherUserId = 8L, otherUserName = "Callee",
            otherUserAvatarUrl = null, callType = "AUDIO", status = "BUSY", createdAt = "2026-10-22T10:00:00",
            answeredAt = null, endedAt = "2026-10-22T10:00:00", durationSeconds = null, outgoing = true
        )
        assertEquals("Cuoc goi audio: Nguoi nhan dang ban", history.toChatMessage().content)
    }

    @Test
    fun missedCallPresentationUsesFailureToneAndFormatsSubMinuteDuration() {
        val history = ApiCallHistory(
            id = 14L,
            callId = "call-44",
            conversationId = 99L,
            otherUserId = 8L,
            otherUserName = "Callee",
            otherUserAvatarUrl = null,
            callType = "VIDEO",
            status = "MISSED",
            createdAt = "2026-10-22T10:00:00",
            answeredAt = null,
            endedAt = "2026-10-22T10:00:42",
            durationSeconds = 42L,
            outgoing = false
        )

        val presentation = history.toChatMessage().callChatPresentation()

        assertEquals("VIDEO", presentation?.callType)
        assertEquals("Cuoc goi nho", presentation?.title)
        assertEquals("42 giay", presentation?.detail)
        assertTrue(presentation?.isProblem == true)
    }

    @Test
    fun heartbeatRunsOnlyAfterTheCallWasAccepted() {
        listOf("CONNECTING", "ACTIVE", "RECONNECTING", "INTERRUPTED").forEach { assertTrue(it, shouldSendCallHeartbeat(it)) }
        listOf("IDLE", "OUTGOING", "INCOMING").forEach { assertFalse(it, shouldSendCallHeartbeat(it)) }
        // Three beats fit inside the backend's 45s timeout, so a lost frame does not end the call.
        assertTrue(CALL_HEARTBEAT_INTERVAL_MS * 3 <= 45_000L)
    }

    private fun callEvent(type: String, callId: String = "call-1") = CallEventModel(
        type = type,
        callId = callId,
        conversationId = 99L,
        callerId = 7L,
        callerName = "Caller",
        calleeId = 8L,
        calleeName = "Callee",
        callType = "AUDIO",
        status = "ENDED",
        actorId = 8L,
        payload = null
    )
}
