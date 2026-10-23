package com.taihoang.socialmobile

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Authenticated STOMP 1.2 client shared by notifications, presence and chat.
 * One instance serves one signed-in account; create a new one when the account changes.
 */
class StompClient(
    private val baseUrl: String,
    private val onNotification: (ApiNotification) -> Unit,
    private val onUnreadCount: (Int) -> Unit,
    private val onPresence: (ApiPresence) -> Unit,
    private val onStateChanged: (Boolean) -> Unit = {},
    private val onConnected: () -> Unit = {},
    private val onChatMessage: (ApiMessage) -> Unit = {},
    private val onChatEvent: (String, JSONObject) -> Unit = { _, _ -> },
    private val onCallEvent: (JSONObject) -> Unit = {},
    /** Latest access token for reconnect attempts, so a refreshed token is picked up without a new client. */
    private val tokenProvider: () -> String? = { null },
    /** The server answered CONNECT with ERROR, almost always because [rejectedToken] is no longer valid. */
    private val onConnectRejected: (rejectedToken: String) -> Unit = {},
    private val onLoginApproval: (ApiPendingLoginApproval) -> Unit = {},
    private val onStoryReaction: (StoryReactionEvent) -> Unit = {}
) {
    private data class PendingFrame(val frame: String)

    companion object {
        private const val MAX_PENDING_FRAMES = 200
        private const val STOMP_HEARTBEAT_MS = 10_000L
        private const val PRESENCE_HEARTBEAT_MS = 30_000L
    }

    private val gson = Gson()
    private val httpClient = OkHttpClient.Builder().retryOnConnectionFailure(true).build()
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private val stateLock = Any()
    private var stompHeartbeat: ScheduledFuture<*>? = null
    private var presenceHeartbeat: ScheduledFuture<*>? = null
    private var socket: WebSocket? = null
    private var connected = false
    private var connecting = false
    private var frameBuffer = ""
    private var accessToken: String? = null
    private var explicitlyDisconnected = true
    private var reconnectAttempt = 0
    private var reconnectTask: ScheduledFuture<*>? = null
    private var connectionGeneration = 0L
    private val pendingFrames = ArrayDeque<PendingFrame>()

    fun connect(token: String) {
        val generation: Long
        synchronized(stateLock) {
            if (!explicitlyDisconnected && accessToken == token && (connected || connecting)) return
            // A different token here is a refreshed token for the same account, so queued frames stay.
            explicitlyDisconnected = false
            accessToken = token
            reconnectAttempt = 0
            reconnectTask?.cancel(false)
            reconnectTask = null
            connectionGeneration += 1
            generation = connectionGeneration
            closeSocketLocked()
            connecting = true
        }
        onStateChanged(false)
        openSocket(token, generation)
    }

    private fun openSocket(token: String, generation: Long) {
        val wsUrl = websocketUrlForBackend(baseUrl)
        val createdSocket = httpClient.newWebSocket(
            Request.Builder().url(wsUrl).build(),
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    val active = synchronized(stateLock) {
                        generation == connectionGeneration && !explicitlyDisconnected
                    }
                    if (!active) {
                        webSocket.close(1000, "closed")
                        return
                    }
                    synchronized(stateLock) {
                        if (generation != connectionGeneration || explicitlyDisconnected) {
                            webSocket.close(1000, "closed")
                            return
                        }
                        socket = webSocket
                    }
                    sendFrame(
                        "CONNECT",
                        linkedMapOf(
                            "accept-version" to "1.2",
                            "host" to stompHostForBackend(baseUrl),
                            "Authorization" to "Bearer $token",
                            "heart-beat" to "10000,30000"
                        )
                    )
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    synchronized(stateLock) {
                        if (generation != connectionGeneration || socket !== webSocket) return
                        frameBuffer += text
                    }
                    drainFrames()
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    if (!markSocketClosed(webSocket, generation)) return
                    webSocket.close(code, reason)
                    scheduleReconnect(generation)
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    if (!markSocketClosed(webSocket, generation)) return
                    scheduleReconnect(generation)
                }
            }
        )
        synchronized(stateLock) {
            if (generation == connectionGeneration && !explicitlyDisconnected) {
                socket = createdSocket
            } else {
                createdSocket.close(1000, "stale connection")
            }
        }
    }

    private fun markSocketClosed(webSocket: WebSocket, generation: Long): Boolean {
        val active = synchronized(stateLock) {
            if (generation != connectionGeneration || socket !== webSocket) return false
            connected = false
            connecting = false
            cancelHeartbeatTasksLocked()
            socket = null
            frameBuffer = ""
            true
        }
        if (active) onStateChanged(false)
        return active
    }

    fun disconnect() {
        synchronized(stateLock) {
            explicitlyDisconnected = true
            accessToken = null
            reconnectTask?.cancel(false)
            reconnectTask = null
            connectionGeneration += 1
            pendingFrames.clear()
            closeSocketLocked()
        }
        onStateChanged(false)
    }

    fun dispose() {
        disconnect()
        scheduler.shutdownNow()
        httpClient.dispatcher.executorService.shutdown()
        httpClient.connectionPool.evictAll()
    }

    private fun closeSocketLocked() {
        cancelHeartbeatTasksLocked()
        socket?.close(1000, "closed")
        socket = null
        connected = false
        connecting = false
        frameBuffer = ""
    }

    private fun cancelHeartbeatTasksLocked() {
        stompHeartbeat?.cancel(false)
        stompHeartbeat = null
        presenceHeartbeat?.cancel(false)
        presenceHeartbeat = null
    }

    private fun scheduleReconnect(generation: Long) {
        val delay: Long
        synchronized(stateLock) {
            if (explicitlyDisconnected || accessToken == null || scheduler.isShutdown || generation != connectionGeneration) return
            reconnectTask?.cancel(false)
            delay = (1_000L shl reconnectAttempt.coerceAtMost(4)).coerceAtMost(30_000L)
            reconnectAttempt += 1
        }
        reconnectTask = scheduler.schedule({
            val latestToken = tokenProvider()?.takeIf { it.isNotBlank() }
            val token = synchronized(stateLock) {
                if (explicitlyDisconnected || generation != connectionGeneration) return@schedule
                if (latestToken != null) accessToken = latestToken
                connecting = true
                accessToken
            } ?: return@schedule
            openSocket(token, generation)
        }, delay, TimeUnit.MILLISECONDS)
    }

    // Not named onConnected: a member function would shadow the onConnected callback property.
    private fun handleConnectedFrame() {
        synchronized(stateLock) {
            if (connected || explicitlyDisconnected) return
            connected = true
            connecting = false
            reconnectAttempt = 0
            reconnectTask?.cancel(false)
            reconnectTask = null
        }
        subscribe("notifications", "/user/queue/notifications")
        subscribe("notification-count", "/user/queue/notifications.unread-count")
        subscribe("presence", "/user/queue/presence")
        subscribe("chat-messages", "/user/queue/messages")
        subscribe("chat-ack", "/user/queue/messages.ack")
        subscribe("chat-delivered", "/user/queue/messages.delivered")
        subscribe("chat-seen", "/user/queue/messages.seen")
        subscribe("chat-updated", "/user/queue/messages.updated")
        subscribe("chat-recalled", "/user/queue/messages.recalled")
        subscribe("chat-deleted", "/user/queue/messages.deleted-for-me")
        subscribe("chat-reaction", "/user/queue/messages.reaction")
        subscribe("chat-typing", "/user/queue/chat.typing")
        subscribe("chat-errors", "/user/queue/errors")
        subscribe("conversation-events", "/user/queue/conversations.events")
        subscribe("login-approvals", "/user/queue/security.login-approvals")
        subscribe("story-reactions", "/user/queue/story.reactions")
        subscribe("calls", "/user/queue/calls")
        synchronized(stateLock) {
            stompHeartbeat = scheduler.scheduleAtFixedRate(
                { if (isConnected()) sendRawFrame("\n") },
                STOMP_HEARTBEAT_MS,
                STOMP_HEARTBEAT_MS,
                TimeUnit.MILLISECONDS
            )
            presenceHeartbeat = scheduler.scheduleAtFixedRate(
                { if (isConnected()) sendFrame("SEND", mapOf("destination" to "/app/presence.heartbeat"), "{}") },
                PRESENCE_HEARTBEAT_MS,
                PRESENCE_HEARTBEAT_MS,
                TimeUnit.MILLISECONDS
            )
        }
        flushPendingFrames()
        onStateChanged(true)
        onConnected()
    }

    fun isConnected(): Boolean = synchronized(stateLock) { connected }

    private fun subscribe(id: String, destination: String) =
        sendFrame("SUBSCRIBE", linkedMapOf("id" to id, "destination" to destination, "ack" to "auto"))

    private fun buildFrame(command: String, headers: Map<String, String>, body: String = ""): String = buildString {
        append(command).append('\n')
        headers.forEach { (key, value) -> append(key).append(':').append(value.replace("\n", "")).append('\n') }
        append('\n').append(body).append('\u0000')
    }

    private fun sendFrame(command: String, headers: Map<String, String>, body: String = ""): Boolean =
        sendRawFrame(buildFrame(command, headers, body))

    private fun sendRawFrame(frame: String): Boolean {
        val currentSocket = synchronized(stateLock) { socket }
        return currentSocket?.send(frame) == true
    }

    private fun sendOrQueueFrame(frame: String): Boolean {
        val currentSocket: WebSocket?
        synchronized(stateLock) {
            currentSocket = if (connected) socket else null
            if (currentSocket == null) {
                if (!explicitlyDisconnected && pendingFrames.size < MAX_PENDING_FRAMES) {
                    pendingFrames.addLast(PendingFrame(frame))
                }
                return false
            }
        }
        val sent = currentSocket?.send(frame) == true
        if (!sent) {
            synchronized(stateLock) {
                if (!explicitlyDisconnected && pendingFrames.size < MAX_PENDING_FRAMES) {
                    pendingFrames.addLast(PendingFrame(frame))
                }
            }
        }
        return sent
    }

    private fun flushPendingFrames() {
        while (true) {
            val pending = synchronized(stateLock) {
                if (!connected || pendingFrames.isEmpty()) null else pendingFrames.removeFirst()
            } ?: return
            if (!sendRawFrame(pending.frame)) {
                synchronized(stateLock) {
                    if (pendingFrames.size < MAX_PENDING_FRAMES) pendingFrames.addFirst(pending)
                }
                return
            }
        }
    }

    private fun drainFrames() {
        while (true) {
            val raw = synchronized(stateLock) {
                frameBuffer = frameBuffer.trimStart('\n', '\r')
                val end = frameBuffer.indexOf('\u0000')
                if (end < 0) return
                val frame = frameBuffer.substring(0, end)
                frameBuffer = frameBuffer.substring(end + 1)
                frame
            }
            if (raw.isBlank()) continue
            val normalized = raw.replace("\r\n", "\n")
            val separator = normalized.indexOf("\n\n")
            if (separator < 0) continue
            val commandAndHeaders = normalized.substring(0, separator).split('\n')
            val command = commandAndHeaders.firstOrNull() ?: continue
            val headers = commandAndHeaders.drop(1).mapNotNull { line ->
                line.indexOf(':').takeIf { it > 0 }?.let { line.substring(0, it) to line.substring(it + 1) }
            }.toMap()
            val body = normalized.substring(separator + 2)
            when (command) {
                "CONNECTED" -> handleConnectedFrame()
                "MESSAGE" -> handleMessage(headers["destination"].orEmpty(), body)
                "ERROR" -> {
                    // Before CONNECTED, ERROR means the handshake (JWT check) was refused.
                    val rejectedToken = synchronized(stateLock) { if (connected) null else accessToken }
                    if (rejectedToken != null) onConnectRejected(rejectedToken)
                    else onChatEvent(
                        "/user/queue/errors",
                        JSONObject().put("code", "BROKER_ERROR").put("message", headers["message"] ?: body)
                    )
                }
            }
        }
    }

    private fun handleMessage(destination: String, body: String) {
        runCatching {
            when (destination) {
                "/user/queue/notifications" -> gson.fromJson(body, ApiNotificationWire::class.java).toApiNotification().let(onNotification)
                "/user/queue/notifications.unread-count" -> gson.fromJson(body, UnreadCountWire::class.java).unreadCount
                    .coerceIn(0L, Int.MAX_VALUE.toLong()).toInt().let(onUnreadCount)
                "/user/queue/presence" -> gson.fromJson(body, PresenceWire::class.java).toApiPresence().let(onPresence)
                "/user/queue/messages", "/user/queue/messages.ack" -> JSONObject(body).toApiMessage()?.let(onChatMessage)
                "/user/queue/messages.delivered", "/user/queue/messages.seen", "/user/queue/messages.updated",
                "/user/queue/messages.recalled", "/user/queue/messages.deleted-for-me", "/user/queue/messages.reaction",
                "/user/queue/chat.typing", "/user/queue/errors", "/user/queue/conversations.events" -> onChatEvent(destination, JSONObject(body))
                "/user/queue/security.login-approvals" -> gson.fromJson(body, LoginApprovalWire::class.java).toPendingApproval()?.let(onLoginApproval)
                "/user/queue/story.reactions" -> gson.fromJson(body, StoryReactionWire::class.java).toEvent()?.let(onStoryReaction)
                "/user/queue/calls" -> onCallEvent(JSONObject(body))
                else -> Unit
            }
        }.onFailure { error ->
            android.util.Log.e("SocialCall", "Failed to parse realtime frame destination=$destination", error)
        }
    }

    fun sendChatMessage(conversationId: Long, content: String, clientMessageId: String, replyToMessageId: Long? = null, uploadIds: List<Long> = emptyList()) {
        sendJson("/app/chat.send", JSONObject().apply {
            put("conversationId", conversationId)
            put("clientMessageId", clientMessageId)
            put("content", content)
            if (replyToMessageId == null) put("replyToMessageId", JSONObject.NULL) else put("replyToMessageId", replyToMessageId)
            put("uploadIds", org.json.JSONArray(uploadIds))
        })
    }

    fun sendDelivered(messageId: Long) = sendJson("/app/chat.delivered", JSONObject().put("messageId", messageId))
    fun sendSeen(messageId: Long) = sendJson("/app/chat.seen", JSONObject().put("messageId", messageId))
    fun sendSeenConversation(conversationId: Long) = sendJson("/app/chat.seenConversation", JSONObject().put("conversationId", conversationId))
    fun sendEdit(messageId: Long, content: String) = sendJson("/app/chat.edit", JSONObject().put("messageId", messageId).put("content", content))
    fun sendRecall(messageId: Long) = sendJson("/app/chat.recall", JSONObject().put("messageId", messageId))
    fun sendDeleteForMe(messageId: Long) = sendJson("/app/chat.deleteForMe", JSONObject().put("messageId", messageId))
    fun sendReaction(messageId: Long, type: String) = sendJson("/app/chat.react", JSONObject().put("messageId", messageId).put("type", type))
    fun sendTyping(conversationId: Long, typing: Boolean) = sendJson("/app/chat.typing", JSONObject().put("conversationId", conversationId).put("typing", typing), queueWhenDisconnected = false)

    fun sendCallInvite(conversationId: Long, calleeId: Long, callType: String) = sendJson(
        "/app/call.invite",
        JSONObject().put("conversationId", conversationId).put("calleeId", calleeId).put("callType", callType)
    )

    // Not queued while offline: a burst of stale heartbeats after reconnect would mean nothing.
    fun sendCallHeartbeat(callId: String) =
        sendJson("/app/call.heartbeat", JSONObject().put("callId", callId), queueWhenDisconnected = false)

    fun sendCallAction(destination: String, callId: String, payload: JSONObject? = null) {
        val body = JSONObject().put("callId", callId)
        if (payload == null) body.put("payload", JSONObject.NULL) else body.put("payload", payload)
        sendJson(destination, body)
    }

    private fun sendJson(destination: String, body: JSONObject, queueWhenDisconnected: Boolean = true): Boolean {
        val frame = buildFrame("SEND", mapOf("destination" to destination, "content-type" to "application/json"), body.toString())
        return if (queueWhenDisconnected) sendOrQueueFrame(frame) else sendRawFrame(frame)
    }

    private data class ApiNotificationWire(val id: Long?, val type: String?, val reactionType: String?, val actorId: Long?, val actorName: String?, val actorAvatarUrl: String?, val referenceId: Long?, val postId: Long?, val commentId: Long?, val storyId: Long?, val message: String?, val read: Boolean = false, val createdAt: String?) {
        fun toApiNotification() = ApiNotification(id, message ?: "Thong bao moi", read, type.orEmpty(), reactionType, actorId, actorName, actorAvatarUrl, referenceId, postId, commentId, storyId, createdAt)
    }

    private data class UnreadCountWire(val unreadCount: Long = 0L)

    private data class PresenceWire(val userId: Long?, val status: String = "OFFLINE", val activeSessionCount: Int = 0, val lastSeenAt: String?) {
        fun toApiPresence() = ApiPresence(userId, status, activeSessionCount, lastSeenAt)
    }

    private data class LoginApprovalWire(val approvalRequestId: Long?, val deviceName: String?, val deviceType: String?, val browser: String?, val os: String?, val ipAddress: String?, val riskScore: Int?, val expiresAt: String?) {
        fun toPendingApproval() = approvalRequestId?.let {
            ApiPendingLoginApproval(it, null, deviceName, deviceType, browser, os, ipAddress, riskScore ?: 0, java.time.LocalDateTime.now().toString(), expiresAt, "PENDING")
        }
    }

    private data class StoryReactionWire(val storyId: Long?, val actorId: Long?, val actorName: String?, val type: String?, val action: String?) {
        fun toEvent() = if (storyId == null || action == null) null else StoryReactionEvent(storyId, actorId, actorName, type, action)
    }
}
