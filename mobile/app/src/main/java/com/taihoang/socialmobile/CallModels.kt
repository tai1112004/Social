package com.taihoang.socialmobile

import org.json.JSONObject

data class CallEventModel(
    val type: String,
    val callId: String,
    val conversationId: Long,
    val callerId: Long,
    val callerName: String,
    val calleeId: Long,
    val calleeName: String,
    val callType: String,
    val status: String,
    val actorId: Long?,
    val payload: JSONObject?,
    val createdAt: String? = null
)

data class PendingCallRequest(
    val conversation: ApiConversation,
    val callType: String,
    val acceptingCall: Boolean = false
)

data class CallIceCandidate(
    val sdpMid: String?,
    val sdpMLineIndex: Int,
    val candidate: String
)

internal class LocalIceCandidateBuffer {
    private val candidates = mutableListOf<CallIceCandidate>()

    fun add(candidate: CallIceCandidate) {
        candidates += candidate
    }

    fun drain(): List<CallIceCandidate> = candidates.toList().also { candidates.clear() }

    fun clear() {
        candidates.clear()
    }
}

enum class CallQualityLevel(val label: String) {
    GOOD("Tot"),
    FAIR("On dinh"),
    POOR("Kem")
}

data class CallQuality(
    val level: CallQualityLevel,
    val rttMs: Int?,
    val packetLossPercent: Int?,
    val jitterMs: Int?
)

internal fun classifyCallQuality(rttMs: Int?, packetLossPercent: Int?, jitterMs: Int?): CallQualityLevel {
    val poor = (rttMs ?: 0) >= 250 || (packetLossPercent ?: 0) >= 5 || (jitterMs ?: 0) >= 30
    if (poor) return CallQualityLevel.POOR
    val fair = (rttMs ?: 0) >= 120 || (packetLossPercent ?: 0) >= 2 || (jitterMs ?: 0) >= 15
    return if (fair) CallQualityLevel.FAIR else CallQualityLevel.GOOD
}

// Backend expiry is 30 seconds; this buffer handles a delayed scheduler event or a lost socket.
const val CALL_RING_TIMEOUT_MS = 40_000L
const val CALL_CONNECT_TIMEOUT_MS = 35_000L

internal fun resolveSignalingCallId(stableCallId: String?, activeCallId: String?): String? =
    stableCallId?.takeIf { it.isNotBlank() } ?: activeCallId?.takeIf { it.isNotBlank() }

internal fun shouldTimeoutCallPhase(phase: String): Boolean =
    phase == "CONNECTING" || phase == "RECONNECTING"

// The backend ends an ACCEPTED call after 45s without a heartbeat or signal (two missed beats).
const val CALL_HEARTBEAT_INTERVAL_MS = 15_000L

/** Phases after the callee accepted, while the backend still counts the call as ongoing. */
internal fun shouldSendCallHeartbeat(phase: String): Boolean =
    phase == "CONNECTING" || phase == "ACTIVE" || phase == "RECONNECTING" || phase == "INTERRUPTED"

/** STOMP error code for an invite the backend refused before it could ring. */
internal const val CALL_INVITE_FAILED_CODE = "CALL_INVITE_FAILED"

internal fun JSONObject.toCallEvent(): CallEventModel? {
    val callId = optStringOrNull("callId") ?: return null
    return CallEventModel(
        type = optStringOr("type", "CALL_UNKNOWN"),
        callId = callId,
        conversationId = optLong("conversationId"),
        callerId = optLong("callerId"),
        callerName = optStringOr("callerName", "Người dùng"),
        calleeId = optLong("calleeId"),
        calleeName = optStringOr("calleeName", "Người dùng"),
        callType = optStringOr("callType", "AUDIO"),
        status = optStringOr("status", "RINGING"),
        actorId = optLong("actorId").takeIf { it != 0L },
        payload = optJSONObject("payload"),
        createdAt = optStringOrNull("createdAt")
    )
}

/**
 * Accepting suspends while the media is prepared; the caller may cancel (or the ring may time out)
 * meanwhile. Only an incoming call that is still the active one may be accepted afterwards.
 */
internal fun canAcceptAfterPrepare(acceptedCallId: String, activeCallId: String?, phase: String): Boolean =
    activeCallId == acceptedCallId && phase == "INCOMING"

internal fun shouldApplyCallEvent(activeCallId: String?, eventCallId: String): Boolean =
    activeCallId.isNullOrBlank() || activeCallId == eventCallId

internal fun CallEventModel.toTerminalChatMessage(now: String): ApiMessage? {
    val outcome = when (type) {
        "CALL_REJECT" -> "REJECTED" to "Cuoc goi bi tu choi"
        "CALL_CANCEL" -> "CANCELLED" to "Cuoc goi da huy"
        "CALL_END" -> "ENDED" to "Cuoc goi da ket thuc"
        "CALL_FAILED" -> "FAILED" to "Cuoc goi that bai"
        "CALL_MISSED" -> "MISSED" to "Cuoc goi nho"
        "CALL_BUSY" -> "BUSY" to "Nguoi nhan dang ban"
        else -> return null
    }
    val kind = if (callType.equals("VIDEO", true)) "video" else "audio"
    return ApiMessage(
        id = null,
        conversationId = conversationId,
        senderId = null,
        senderName = "Cuoc goi",
        content = "Cuoc goi $kind: ${outcome.second}",
        messageType = "SYSTEM",
        sentAt = createdAt ?: now,
        editedAt = null,
        recalledAt = null,
        seenAt = null,
        clientMessageId = "call:$callId",
        sequenceNumber = null,
        callType = callType,
        callStatus = outcome.first
    )
}

internal fun ApiCallHistory.toChatMessage(): ApiMessage {
    val kind = if (callType.equals("VIDEO", true)) "video" else "audio"
    val description = when (status.uppercase()) {
        "REJECTED" -> "Cuoc goi bi tu choi"
        "CANCELLED" -> "Cuoc goi da huy"
        "MISSED" -> "Cuoc goi nho"
        "BUSY" -> "Nguoi nhan dang ban"
        "FAILED" -> "Cuoc goi that bai"
        "ACCEPTED" -> "Cuoc goi da nhan"
        "ENDED" -> "Cuoc goi da ket thuc"
        else -> "Cuoc goi dang do chuong"
    }
    val duration = durationSeconds?.takeIf { it > 0 }?.let { " (${it}s)" }.orEmpty()
    return ApiMessage(
        id = null,
        conversationId = conversationId,
        senderId = null,
        senderName = "Cuoc goi",
        content = "Cuoc goi $kind: $description$duration",
        messageType = "SYSTEM",
        sentAt = endedAt ?: answeredAt ?: createdAt,
        editedAt = null,
        recalledAt = null,
        seenAt = null,
        clientMessageId = "call:$callId",
        sequenceNumber = null,
        callType = callType,
        callStatus = status,
        callDurationSeconds = durationSeconds,
        callOutgoing = outgoing
    )
}

internal data class CallChatPresentation(
    val callType: String,
    val title: String,
    val detail: String,
    val isProblem: Boolean
)

/** Produces the compact visual model for a call record embedded in a chat timeline. */
internal fun ApiMessage.callChatPresentation(): CallChatPresentation? {
    val type = callType?.uppercase() ?: return null
    if (clientMessageId?.startsWith("call:") != true) return null

    val status = callStatus.orEmpty().uppercase()
    val typeTitle = if (type == "VIDEO") "Cuoc goi video" else "Cuoc goi am thanh"
    val title = when (status) {
        "MISSED" -> "Cuoc goi nho"
        "REJECTED" -> "Cuoc goi bi tu choi"
        "CANCELLED" -> "Cuoc goi da huy"
        "BUSY" -> "Nguoi nhan dang ban"
        "FAILED" -> "Cuoc goi that bai"
        else -> typeTitle
    }
    val direction = when (callOutgoing) {
        true -> "Cuoc goi di"
        false -> "Cuoc goi den"
        null -> null
    }
    val duration = formatCallDuration(callDurationSeconds)
    val statusDetail = when (status) {
        "ENDED" -> "Da ket thuc"
        "ACCEPTED" -> "Da nhan"
        "RINGING" -> "Dang do chuong"
        else -> null
    }
    return CallChatPresentation(
        callType = type,
        title = title,
        detail = duration ?: listOfNotNull(direction, statusDetail).joinToString(" - ").ifBlank { "Cuoc goi" },
        isProblem = status in setOf("MISSED", "REJECTED", "CANCELLED", "BUSY", "FAILED")
    )
}

internal fun formatCallDuration(seconds: Long?): String? {
    val totalSeconds = seconds?.takeIf { it > 0 } ?: return null
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return when {
        minutes == 0L -> "$remainingSeconds giay"
        remainingSeconds == 0L -> "$minutes phut"
        else -> "$minutes phut $remainingSeconds giay"
    }
}
