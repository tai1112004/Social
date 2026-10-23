package com.taihoang.socialmobile

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import java.io.ByteArrayOutputStream
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.URL
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import java.time.OffsetDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

private val Background = Color(0xFFF0F2F5)
private val Ink = Color(0xFF111827)
private val Muted = Color(0xFF65676B)
private val Brand = Color(0xFF1877F2)
private val SoftBrand = Color(0xFFE7F3FF)

private data class ChatRealtimeEvent(val destination: String, val payload: org.json.JSONObject, val nonce: Long = System.nanoTime())

private fun chatReactionEmoji(type: String): String = when (type.uppercase()) {
    "LOVE" -> "\uD83D\uDC96"
    "HAHA" -> "\uD83D\uDE02"
    "WOW" -> "\uD83D\uDE2E"
    "SAD" -> "\uD83D\uDE22"
    "ANGRY" -> "\uD83D\uDE21"
    else -> "\uD83D\uDC4D"
}

internal val POST_REACTION_TYPES = listOf("LIKE", "LOVE", "HAHA", "SAD", "WOW")

private fun notificationDestinationTab(notification: ApiNotification): Int = when (notification.type.uppercase()) {
    "FRIEND_REQUEST", "FRIEND_ACCEPTED" -> 2
    "NEW_MESSAGE", "MESSAGE" -> 3
    "SECURITY_LOGIN" -> 5
    else -> 0
}

/** One playback authority for every video surface in the app. */
private object VideoPlaybackManager {
    private val players = linkedMapOf<String, VideoView>()
    private var currentPlayingId: String? = null

    fun register(id: String, view: VideoView) {
        val previous = players.put(id, view)
        if (previous != null && previous !== view && previous.isPlaying) {
            previous.pause()
            android.util.Log.d("VideoPlayback", "PAUSE id=$id (replaced view)")
        }
    }
    fun unregister(id: String, view: VideoView) {
        if (players[id] === view && view.isPlaying) {
            view.pause()
            android.util.Log.d("VideoPlayback", "PAUSE id=$id (unregister)")
        }
        if (currentPlayingId == id) currentPlayingId = null
        if (players[id] === view) players.remove(id)
    }
    fun play(id: String) {
        // Defensive sweep: a stale/recycled view must never keep playing.
        players.forEach { (playerId, view) ->
            if (playerId != id && view.isPlaying) {
                view.pause()
                android.util.Log.d("VideoPlayback", "PAUSE id=$playerId")
            }
        }
        currentPlayingId = null
        players[id]?.let {
            val transition = !it.isPlaying
            if (!it.isPlaying) it.start()
            currentPlayingId = id
            if (transition) android.util.Log.d("VideoPlayback", "PLAY id=$id")
        }
    }
    fun pause(id: String) {
        players[id]?.let {
            if (it.isPlaying) {
                it.pause()
                android.util.Log.d("VideoPlayback", "PAUSE id=$id")
            }
        }
        if (currentPlayingId == id) currentPlayingId = null
    }
    fun pauseAll() {
        players.forEach { (id, view) ->
            if (view.isPlaying) {
                view.pause()
                android.util.Log.d("VideoPlayback", "PAUSE id=$id")
            }
        }
        currentPlayingId = null
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = SessionStore(this)
        val endpoint = backendEndpointForId(store.selectedBackendEndpointId(BuildConfig.API_BASE_URL))
            ?: LOCAL_BACKEND_ENDPOINT
        setContent { SocialMobileApp(ApiClient(endpoint.baseUrl, store), store) }
    }
    override fun onPause() { VideoPlaybackManager.pauseAll(); super.onPause() }
    override fun onStop() { VideoPlaybackManager.pauseAll(); super.onStop() }
}

@Composable
fun SocialMobileApp(api: ApiClient, store: SessionStore) {
    var session by remember { mutableStateOf(store.get()) }
    var selectedBackendEndpointId by remember { mutableStateOf(backendEndpointIdForUrl(api.baseUrl)) }
    var selectedTab by remember { mutableStateOf(0) }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var conversations by remember { mutableStateOf<List<ApiConversation>>(emptyList()) }
    var friends by remember { mutableStateOf<List<ApiFriend>>(emptyList()) }
    var optimisticFriendIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var notifications by remember { mutableStateOf<List<ApiNotification>>(emptyList()) }
    var unreadNotificationCount by remember { mutableStateOf(0) }
    var notificationPage by remember { mutableStateOf(0) }
    var notificationTotalPages by remember { mutableStateOf(0) }
    var notificationLoading by remember { mutableStateOf(false) }
    var notificationError by remember { mutableStateOf<String?>(null) }
    var realtimeConnected by remember { mutableStateOf(false) }
    var realtimeResyncVersion by remember { mutableStateOf(0L) }
    var presenceByUser by remember { mutableStateOf<Map<Long, ApiPresence>>(emptyMap()) }
    var chatMessages by remember { mutableStateOf<List<ApiMessage>>(emptyList()) }
    var chatEvents by remember { mutableStateOf<List<ChatRealtimeEvent>>(emptyList()) }
    var chatTypingByConversation by remember { mutableStateOf<Map<Long, Long>>(emptyMap()) }
    var activeChatConversationId by remember { mutableStateOf<Long?>(null) }
    val stompRef = remember { mutableStateOf<StompClient?>(null) }
    var profile by remember { mutableStateOf<ApiProfile?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var showTrustPrompt by remember { mutableStateOf(false) }
    var showSecuritySettings by remember { mutableStateOf(false) }
    var showMyPosts by remember { mutableStateOf(false) }
    var showSavedPosts by remember { mutableStateOf(false) }
    var showMyReports by remember { mutableStateOf(false) }
    var showAdmin by remember { mutableStateOf(false) }
    var viewedProfileUserId by remember { mutableStateOf<Long?>(null) }
    var pendingApproval by remember { mutableStateOf<AuthSession?>(null) }
    var loginApprovalPrompts by remember { mutableStateOf<List<ApiPendingLoginApproval>>(emptyList()) }
    var loginApprovalBusy by remember { mutableStateOf(false) }
    var loginApprovalError by remember { mutableStateOf<String?>(null) }
    var storyReactionEvents by remember { mutableStateOf<List<StoryReactionEvent>>(emptyList()) }
    val seenNotificationIds = remember { mutableStateOf<Set<Long>>(emptySet()) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val context = LocalContext.current
    val callManager = remember { WebRtcCallManager(context) }
    var activeCall by remember { mutableStateOf<CallEventModel?>(null) }
    var callPhase by remember { mutableStateOf("IDLE") }
    var callError by remember { mutableStateOf<String?>(null) }
    var callMuted by remember { mutableStateOf(false) }
    var callCameraEnabled by remember { mutableStateOf(true) }
    var callFrontCamera by remember { mutableStateOf(true) }
    var callAudioRoute by remember { mutableStateOf(CallAudioRoute.EARPIECE) }
    var callInterruptionReason by remember { mutableStateOf<String?>(null) }
    var callQuality by remember { mutableStateOf<CallQuality?>(null) }
    var callMediaVersion by remember { mutableStateOf(0L) }
    var pendingCallRequest by remember { mutableStateOf<PendingCallRequest?>(null) }
    val callReconnectJob = remember { mutableStateOf<Job?>(null) }
    val pendingLocalIceCandidates = remember { LocalIceCandidateBuffer() }
    // Keep signaling identity stable while Compose state is waiting for its next recomposition.
    var signalingCallId by remember { mutableStateOf<String?>(null) }
    var showCallHistory by remember { mutableStateOf(false) }
    var callHistory by remember { mutableStateOf<List<ApiCallHistory>>(emptyList()) }
    var presenceRefreshToken by remember { mutableStateOf(0L) }
    var callNotificationPermissionRequested by remember { mutableStateOf(false) }
    val callNotificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val currentCallState = rememberUpdatedState(activeCall)
    val currentCallPhase = rememberUpdatedState(callPhase)
    val currentActiveChatState = rememberUpdatedState(activeChatConversationId)
    val refreshMutex = remember { Mutex() }
    // Stays the same when the access token is refreshed, so data and the socket are not rebuilt.
    val accountKey = session?.let { it.userId ?: it.email ?: it.token }

    suspend fun refreshPresence(userIds: Set<Long>) {
        val states = coroutineScope {
            userIds.map { userId ->
                async { runCatching { api.presence(userId) }.getOrNull() }
            }.awaitAll()
        }
        val mergedPresence = presenceByUser.toMutableMap()
        states.filterNotNull().forEach { state ->
            state.userId?.let { id -> mergedPresence[id] = state }
        }
        presenceByUser = mergedPresence
    }

    suspend fun refreshFriendsAndConversations() {
        refreshMutex.withLock {
            val loadedFriends = api.friends()
            val loadedConversations = api.conversationPage(limit = 50).items
            val retainedOptimisticFriends = friends.filter { it.id in optimisticFriendIds }
            val mergedFriends = (loadedFriends + retainedOptimisticFriends).distinctBy { it.id }
            friends = mergedFriends
            optimisticFriendIds = optimisticFriendIds - loadedFriends.map { it.id }.toSet()
            conversations = mergeConversationSnapshots(
                conversations,
                loadedConversations,
                currentActiveChatState.value
            )
            refreshPresence(presenceUserIds(mergedFriends, conversations))
            presenceRefreshToken += 1L
        }
    }

    fun selectBackendEndpoint(endpoint: BackendEndpoint) {
        store.saveBackendEndpoint(endpoint.id)
        api.updateBaseUrl(endpoint.baseUrl)
        selectedBackendEndpointId = endpoint.id
        error = null
    }

    fun finishCall() {
        val endedCallId = activeCall?.callId?.takeIf { it.isNotBlank() }
        callReconnectJob.value?.cancel()
        callReconnectJob.value = null
        activeCall = null
        signalingCallId = null
        callPhase = "IDLE"
        callMuted = false
        callCameraEnabled = true
        callFrontCamera = true
        callAudioRoute = CallAudioRoute.EARPIECE
        callInterruptionReason = null
        callQuality = null
        pendingCallRequest = null
        pendingLocalIceCandidates.clear()
        callManager.reset()
        CallForegroundService.stop(context)
        scope.launch {
            repeat(3) { attempt ->
                if (attempt > 0) delay(500L)
                runCatching { api.callHistory() }.onSuccess { refreshed ->
                    callHistory = refreshed
                    if (endedCallId == null || refreshed.any { it.callId == endedCallId }) return@launch
                }
            }
        }
    }

    fun appendCallChatMessage(event: CallEventModel) {
        val message = event.toTerminalChatMessage(java.time.LocalDateTime.now().toString()) ?: return
        chatMessages = mergeChatMessages(chatMessages, listOf(message))
        conversations.firstOrNull { it.id == event.conversationId }?.let { current ->
            conversations = listOf(
                current.copy(
                    lastMessage = message.content,
                    lastMessageSenderName = message.senderName,
                    updatedAt = message.sentAt
                )
            ) + conversations.filterNot { it.id == event.conversationId }
        }
    }

    fun finishCallWithEvent(type: String, destination: String) {
        activeCall?.let { call ->
            appendCallChatMessage(call.copy(type = type))
            stompRef.value?.sendCallAction(destination, call.callId)
        }
        finishCall()
    }

    LaunchedEffect(activeCall?.callId, callPhase) {
        val call = activeCall
        if (call?.callId?.isNotBlank() == true && shouldRunCallForegroundService(callPhase)) {
            if (
                android.os.Build.VERSION.SDK_INT >= 33 &&
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
                !callNotificationPermissionRequested
            ) {
                callNotificationPermissionRequested = true
                callNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
            val title = if (callPhase == "INCOMING") call.callerName else call.calleeName
            CallForegroundService.start(context, title, callPhase, call.callType)
        } else {
            CallForegroundService.stop(context)
        }
    }

    LaunchedEffect(activeCall?.callId, callPhase) {
        val call = activeCall
        if (call == null || call.callId.isBlank() || callPhase !in setOf("INCOMING", "OUTGOING")) return@LaunchedEffect
        val callId = call.callId
        val phaseAtStart = callPhase
        delay(CALL_RING_TIMEOUT_MS)
        if (activeCall?.callId == callId && callPhase == phaseAtStart) {
            callError = "Cuoc goi khong co phan hoi"
            finishCall()
        }
    }

    LaunchedEffect(activeCall?.callId, callPhase) {
        val call = activeCall
        if (call == null || call.callId.isBlank() || !shouldTimeoutCallPhase(callPhase)) return@LaunchedEffect
        val callId = call.callId
        delay(CALL_CONNECT_TIMEOUT_MS)
        if (activeCall?.callId == callId && shouldTimeoutCallPhase(callPhase)) {
            callError = "Khong the ket noi cuoc goi"
            finishCallWithEvent("CALL_FAILED", "/app/call.failed")
        }
    }

    // CallOverlay only renders while a call has an id, so errors raised before the backend
    // assigned one (busy, refused invite) or after the call ended would otherwise never show.
    LaunchedEffect(callError, activeCall?.callId) {
        val message = callError
        if (message != null && activeCall?.callId.isNullOrBlank()) {
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
            callError = null
        }
    }

    // Without it the backend treats the call as abandoned after 45s and ends it.
    LaunchedEffect(activeCall?.callId, callPhase) {
        val callId = activeCall?.callId
        if (callId.isNullOrBlank() || !shouldSendCallHeartbeat(callPhase)) return@LaunchedEffect
        while (true) {
            stompRef.value?.sendCallHeartbeat(callId)
            delay(CALL_HEARTBEAT_INTERVAL_MS)
        }
    }

    fun startCallReconnect(call: CallEventModel) {
        if (callReconnectJob.value?.isActive == true) return
        callReconnectJob.value = scope.launch {
            repeat(CALL_RECONNECT_MAX_ATTEMPTS) { attempt ->
                val current = currentCallState.value
                if (current?.callId != call.callId) return@launch
                callPhase = "RECONNECTING"
                stompRef.value?.sendCallAction(
                    "/app/call.reconnect",
                    call.callId,
                    org.json.JSONObject().put("attempt", attempt + 1)
                )
                delay(callReconnectDelayMs(attempt))
                if (currentCallPhase.value == "ACTIVE") return@launch
            }
            if (currentCallState.value?.callId == call.callId && currentCallPhase.value == "RECONNECTING") {
                callError = "Khong the khoi phuc ket noi cuoc goi"
                stompRef.value?.sendCallAction("/app/call.failed", call.callId)
                finishCall()
            }
        }
    }

    fun sendIceCandidate(callId: String, candidate: CallIceCandidate) {
        stompRef.value?.sendCallAction(
            "/app/call.ice",
            callId,
            org.json.JSONObject()
                .put("sdpMid", candidate.sdpMid)
                .put("sdpMLineIndex", candidate.sdpMLineIndex)
                .put("candidate", candidate.candidate)
        )
    }

    fun flushPendingIceCandidates(callId: String) {
        pendingLocalIceCandidates.drain().forEach { candidate -> sendIceCandidate(callId, candidate) }
    }

    DisposableEffect(callManager) {
        callManager.onLocalVideoReady = { scope.launch { callMediaVersion += 1L } }
        callManager.onRemoteVideoReady = { scope.launch { callMediaVersion += 1L } }
        callManager.onCameraFacingChanged = { isFrontCamera -> scope.launch { callFrontCamera = isFrontCamera } }
        callManager.onAudioRouteChanged = { route -> scope.launch { callAudioRoute = route } }
        callManager.onCallInterruptionChanged = { interrupted, reason ->
            scope.launch {
                callInterruptionReason = reason
                if (interrupted && activeCall != null) {
                    callPhase = "INTERRUPTED"
                } else if (!interrupted && callPhase == "INTERRUPTED") {
                    callPhase = "ACTIVE"
                }
            }
        }
        callManager.onCallQualityChanged = { quality -> scope.launch { callQuality = quality } }
        callManager.onConnectionStateChanged = { state ->
            android.util.Log.d("SocialCall", "connection state=$state call=${currentCallState.value?.callId}")
            scope.launch {
                if (state == "CONNECTED" || state == "COMPLETED") {
                    callReconnectJob.value?.cancel()
                    callReconnectJob.value = null
                    if (currentCallState.value != null) callPhase = "ACTIVE"
                } else if (isRecoverableCallState(state)) {
                    currentCallState.value?.let { call -> startCallReconnect(call) }
                } else if (state == "CLOSED") {
                    callError = "Ket noi cuoc goi da mat"
                }
            }
        }
        callManager.onFailure = { message ->
            android.util.Log.e("SocialCall", "WebRTC failure: $message")
            scope.launch { callError = message }
        }
        callManager.onIceCandidateReady = { candidate ->
            scope.launch {
                val pending = CallIceCandidate(candidate.sdpMid, candidate.sdpMLineIndex, candidate.sdp)
                // Do not rely only on rememberUpdatedState here: offer creation can emit ICE
                // before the recomposition that follows CALL_ACCEPT has completed.
                val callId = resolveSignalingCallId(signalingCallId, activeCall?.callId)
                if (callId.isNullOrBlank()) pendingLocalIceCandidates.add(pending)
                else sendIceCandidate(callId, pending)
            }
        }
        onDispose {
            CallForegroundService.stop(context)
            callManager.release()
        }
    }

    suspend fun prepareCall(request: PendingCallRequest) {
        callError = null
        if (request.acceptingCall) {
            val call = activeCall ?: return
            signalingCallId = call.callId.takeIf { it.isNotBlank() }
            callManager.prepare(call.callType, api.iceServers())
            if (!canAcceptAfterPrepare(call.callId, activeCall?.callId, callPhase)) {
                // Cancelled or timed out while the media was being prepared: the call is already
                // finished here, so only release what prepare just opened.
                callManager.reset()
                return
            }
            callPhase = "CONNECTING"
            stompRef.value?.sendCallAction("/app/call.accept", call.callId)
            return
        }
        val detail = api.conversationDetail(request.conversation.id).toConversationDetail()
        val myId = api.currentUserId()
        val callee = detail.members.firstOrNull { it.userId != myId }
            ?: error("Khong tim thay nguoi nhan cuoc goi")
        callManager.prepare(request.callType, api.iceServers())
        activeCall = CallEventModel(
            type = "CALL_PREPARING",
            callId = "",
            conversationId = request.conversation.id,
            callerId = myId ?: 0L,
            callerName = session?.userName ?: "Ban",
            calleeId = callee.userId,
            calleeName = callee.userName,
            callType = request.callType,
            status = "RINGING",
            actorId = myId,
            payload = null
        )
        callPhase = "OUTGOING"
        stompRef.value?.sendCallInvite(request.conversation.id, callee.userId, request.callType)
    }

    val callPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        val request = pendingCallRequest
        pendingCallRequest = null
        val requiredGranted = grants
            .filterKeys { it != android.Manifest.permission.READ_PHONE_STATE }
            .values
            .all { it }
        if (request != null && requiredGranted) {
            scope.launch { runCatching { prepareCall(request) }.onFailure { callError = it.message ?: "Khong the bat dau cuoc goi" } }
        } else if (request != null) {
            callError = "Can cap quyen microphone${if (request.callType.equals("VIDEO", true)) " va camera" else ""}"
        }
    }

    fun requestCall(request: PendingCallRequest) {
        val permissions = buildList {
            add(android.Manifest.permission.RECORD_AUDIO)
            add(android.Manifest.permission.READ_PHONE_STATE)
            if (request.callType.equals("VIDEO", true)) add(android.Manifest.permission.CAMERA)
        }
        val missing = permissions.filter { androidx.core.content.ContextCompat.checkSelfPermission(context, it) != android.content.pm.PackageManager.PERMISSION_GRANTED }
        if (missing.isEmpty()) {
            scope.launch { runCatching { prepareCall(request) }.onFailure { callError = it.message ?: "Khong the bat dau cuoc goi" } }
        } else {
            pendingCallRequest = request
            callPermissionLauncher.launch(missing.toTypedArray())
        }
    }

    suspend fun handleCallEvent(event: CallEventModel) {
        android.util.Log.d("SocialCall", "event=${event.type} call=${event.callId} actor=${event.actorId} phase=$callPhase")
        if (!shouldApplyCallEvent(activeCall?.callId, event.callId)) return
        signalingCallId = event.callId
        val myId = api.currentUserId()
        when (event.type) {
            "CALL_INVITE" -> {
                activeCall = event
                callPhase = if (event.calleeId == myId) "INCOMING" else "OUTGOING"
            }
            "CALL_ACCEPT" -> {
                activeCall = event
                callPhase = "CONNECTING"
                flushPendingIceCandidates(event.callId)
                if (event.callerId == myId && event.actorId == event.calleeId) {
                    callManager.createOffer { offer ->
                        stompRef.value?.sendCallAction("/app/call.offer", event.callId, org.json.JSONObject().put("type", offer.type.canonicalForm()).put("sdp", offer.description))
                    }
                }
            }
            "CALL_OFFER" -> {
                activeCall = event
                callPhase = "CONNECTING"
                val payload = event.payload ?: return
                callManager.setRemoteDescription(payload.optString("type", "offer"), payload.optString("sdp")) {
                    callManager.createAnswer { answer ->
                        stompRef.value?.sendCallAction("/app/call.answer", event.callId, org.json.JSONObject().put("type", answer.type.canonicalForm()).put("sdp", answer.description))
                    }
                }
            }
            "CALL_ANSWER" -> {
                activeCall = event
                event.payload?.let { payload ->
                    callManager.setRemoteDescription(payload.optString("type", "answer"), payload.optString("sdp"))
                }
            }
            "CALL_ICE_CANDIDATE" -> {
                event.payload?.let { payload ->
                    callManager.addIceCandidate(payload.optStringOrNull("sdpMid"), payload.optInt("sdpMLineIndex", 0), payload.optStringOr("candidate", ""))
                }
            }
            "CALL_REJECT", "CALL_CANCEL", "CALL_END", "CALL_FAILED", "CALL_MISSED", "CALL_BUSY" -> {
                appendCallChatMessage(event)
                finishCall()
                if (event.type == "CALL_BUSY") callError = "Nguoi nhan dang trong mot cuoc goi khac"
            }
            "CALL_RECONNECT" -> {
                callReconnectJob.value?.cancel()
                callReconnectJob.value = null
                activeCall = event
                callPhase = "RECONNECTING"
                callManager.createIceRestartOffer { offer ->
                    stompRef.value?.sendCallAction(
                        "/app/call.offer",
                        event.callId,
                        org.json.JSONObject().put("type", offer.type.canonicalForm()).put("sdp", offer.description)
                    )
                }
            }
        }
    }

    LaunchedEffect(showCallHistory) {
        if (showCallHistory) runCatching { callHistory = api.callHistory() }
    }

    DisposableEffect(api) {
        api.onSessionRefreshed = { refreshed -> scope.launch {
            session = refreshed
            stompRef.value?.takeIf { !it.isConnected() }?.connect(refreshed.token)
        } }
        api.onSessionExpired = { scope.launch { session = null } }
        onDispose {
            api.onSessionRefreshed = null
            api.onSessionExpired = null
        }
    }

    suspend fun syncNotifications(replaceExisting: Boolean) {
        runCatching { api.notifications() }
            .onSuccess { page ->
                val merged = if (replaceExisting) page.items else (page.items + notifications)
                notifications = merged.distinctBy { it.id ?: it.hashCode().toLong() }
                seenNotificationIds.value = seenNotificationIds.value + page.items.mapNotNull { it.id }
                notificationPage = page.page
                notificationTotalPages = page.totalPages
                notificationError = null
            }
            .onFailure { failure ->
                notificationError = failure.message ?: "Không thể tải thông báo"
                if (failure is ApiException && (failure.status == 401 || failure.status == 403)) {
                    store.clear()
                    session = null
                }
            }

        runCatching { api.unreadNotifications() }
            .onSuccess { unreadNotificationCount = it }
            .onFailure { failure ->
                if (notificationError == null) {
                    notificationError = failure.message ?: "Không thể tải số thông báo chưa đọc"
                }
                if (failure is ApiException && (failure.status == 401 || failure.status == 403)) {
                    store.clear()
                    session = null
                }
            }
    }

    suspend fun syncUndeliveredChat() {
        var afterMessageId: Long? = null
        repeat(20) {
            val page = api.chatSyncPage(afterMessageId = afterMessageId, limit = 100)
            chatMessages = mergeChatMessages(chatMessages, page.items)
            incomingMessageIds(page.items, api.currentUserId()).forEach { messageId ->
                stompRef.value?.sendDelivered(messageId)
            }
            if (!page.hasMore || page.nextAfterMessageId == null) return
            afterMessageId = page.nextAfterMessageId
        }
    }

    LaunchedEffect(accountKey) {
        loginApprovalPrompts = emptyList()
        storyReactionEvents = emptyList()
        if (session != null) runCatching {
            posts = api.feed().map { it.toPost() }
            runCatching { api.callHistory() }.onSuccess { callHistory = it }
            runCatching { syncUndeliveredChat() }
            refreshFriendsAndConversations()
            syncNotifications(replaceExisting = true)
            profile = api.profile()
        }.onFailure {
            error = it.message
            if (it is ApiException && (it.status == 401 || it.status == 403)) {
                store.clear()
                session = null
            }
        }
    }

    LaunchedEffect(accountKey) {
        while (session != null) {
            delay(30_000L)
            runCatching { refreshFriendsAndConversations() }
        }
    }

    val currentNotificationsState = rememberUpdatedState(notifications)
    val currentFriendsState = rememberUpdatedState(friends)
    val currentConversationsState = rememberUpdatedState(conversations)
    DisposableEffect(accountKey) {
        val token = session?.token
        if (token == null) return@DisposableEffect onDispose { }
        var hasConnectedOnce = false
        val stomp = StompClient(
            baseUrl = api.baseUrl,
            tokenProvider = { api.currentToken() },
            onConnectRejected = { rejectedToken -> scope.launch {
                // A refreshed session reconnects through onSessionRefreshed; one refreshed by another
                // caller is returned without that callback, so reconnect here as well.
                runCatching { api.refreshSession(rejectedToken) }.getOrNull()?.let { current ->
                    stompRef.value?.takeIf { !it.isConnected() }?.connect(current.token)
                }
            } },
            onLoginApproval = { approval -> scope.launch {
                if (loginApprovalPrompts.none { it.id == approval.id }) loginApprovalPrompts = (listOf(approval) + loginApprovalPrompts).take(3)
            } },
            onStoryReaction = { event -> scope.launch { storyReactionEvents = (storyReactionEvents + event).takeLast(50) } },
            onNotification = { notification -> scope.launch {
                // The callback outlives individual recompositions; always inspect the latest list.
                val latestNotifications = currentNotificationsState.value
                val id = notification.id
                val isNew = if (id == null) true else if (id in seenNotificationIds.value) false else {
                    seenNotificationIds.value = seenNotificationIds.value + id
                    true
                }
                val alreadyVisible = id != null && latestNotifications.any { it.id == id }
                if (alreadyVisible) {
                    // Reaction changes reuse the notification id; replace the visible row in place.
                    notifications = latestNotifications.map { item -> if (item.id == id) notification else item }
                } else {
                    // An event can arrive for an older page that is not loaded yet. Keep it visible
                    // instead of dropping it; the unread-count stream remains authoritative.
                    notifications = listOf(notification) + latestNotifications
                    if (isNew && !notification.read) unreadNotificationCount += 1
                }
                if (notification.type.equals("FRIEND_ACCEPTED", ignoreCase = true)) {
                    runCatching { refreshFriendsAndConversations() }
                }
            } },
            onUnreadCount = { count -> scope.launch { unreadNotificationCount = count } },
            onPresence = { presence -> scope.launch { presence.userId?.let { id -> presenceByUser = presenceByUser + (id to presence) } } },
            onStateChanged = { connected -> scope.launch {
                realtimeConnected = connected
                if (!connected) chatTypingByConversation = emptyMap()
            } },
            onConnected = {
                val reconnecting = hasConnectedOnce
                hasConnectedOnce = true
                scope.launch {
                    runCatching { syncUndeliveredChat() }
                    if (reconnecting) realtimeResyncVersion += 1L
                    runCatching { refreshFriendsAndConversations() }
                    syncNotifications(replaceExisting = false)
                }
            },
            onChatMessage = { message -> scope.launch {
                 incomingMessageIds(listOf(message), api.currentUserId()).forEach { messageId ->
                     stompRef.value?.sendDelivered(messageId)
                 }
                chatMessages = mergeChatMessages(chatMessages, listOf(message))
                message.conversationId?.let { conversationId ->
                    if (conversations.none { it.id == conversationId }) {
                        runCatching { conversations = api.conversationPage(limit = 50).items }
                    }
                    val updatedConversations = updateConversationAfterMessage(
                        conversations,
                        message,
                        session?.userId,
                        currentActiveChatState.value
                    )
                    if (updatedConversations != null) {
                        conversations = updatedConversations
                    } else conversations.firstOrNull { it.id == conversationId }?.let { current ->
                        val updated = current.copy(
                            lastMessage = message.content.ifBlank { if (message.attachments.isNotEmpty()) "Tệp đính kèm" else current.lastMessage },
                            lastMessageId = message.id ?: current.lastMessageId,
                            lastMessageSenderName = message.senderName,
                            updatedAt = message.sentAt,
                            unreadCount = if (message.senderId != session?.userId && conversationId != currentActiveChatState.value) {
                                current.unreadCount + 1
                            } else if (conversationId == currentActiveChatState.value) {
                                0
                            } else current.unreadCount
                        )
                        conversations = listOf(updated) + conversations.filterNot { it.id == conversationId }
                    }
                }
            } },
            onChatEvent = { destination, payload -> scope.launch {
                chatEvents = (chatEvents + ChatRealtimeEvent(destination, payload)).takeLast(100)
                if (destination == "/user/queue/chat.typing") {
                    val conversationId = payload.optLong("conversationId").takeIf { it != 0L }
                    if (conversationId != null) {
                        chatTypingByConversation = if (payload.optBoolean("typing")) {
                            chatTypingByConversation + (conversationId to System.currentTimeMillis())
                        } else {
                            chatTypingByConversation - conversationId
                        }
                    }
                }
                // The backend refused the invite before it could ring; stop the "calling..." screen
                // now instead of after the ringing timeout.
                if (
                    destination == "/user/queue/errors" &&
                    payload.optStringOrNull("code") == CALL_INVITE_FAILED_CODE &&
                    callPhase == "OUTGOING" && activeCall?.callId.isNullOrBlank()
                ) {
                    finishCall()
                    callError = payload.optStringOr("message", "Khong the bat dau cuoc goi")
                }
                if (destination == "/user/queue/conversations.events") {
                    payload.toGroupConversationEvent()?.let { event ->
                        val result = applyGroupEvent(conversations, event, session?.userId)
                        conversations = result.conversations
                        if (result.reloadConversations) runCatching { api.conversations() }.onSuccess { conversations = it }
                    }
                }
            } },
            onCallEvent = { payload -> scope.launch { runCatching { payload.toCallEvent()?.let { handleCallEvent(it) } }.onFailure { callError = it.message } } }
        )
        stompRef.value = stomp
        stomp.connect(token)
        onDispose { stomp.dispose(); stompRef.value = null }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, session?.token) {
        val observer = LifecycleEventObserver { _, event ->
            val activeToken = session?.token
            if (event == Lifecycle.Event.ON_START && activeToken != null) {
                scope.launch {
                    val stomp = stompRef.value
                    if (stomp?.isConnected() != true) stomp?.connect(activeToken)
                    runCatching { syncUndeliveredChat() }
                    runCatching { refreshPresence(presenceUserIds(currentFriendsState.value, currentConversationsState.value)) }
                    syncNotifications(replaceExisting = false)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            Box(Modifier.fillMaxSize()) {
            if (session != null) {
                if (showAdmin) {
                    AdminScreen(api, onBack = { showAdmin = false })
                } else if (showSecuritySettings) {
                    SecuritySettingsScreen(api, onBack = { showSecuritySettings = false }, onLogout = { store.clear(); session = null; showSecuritySettings = false })
                } else if (showMyPosts) {
                    UserPostsScreen(api, profile?.id ?: session?.userId ?: 0L, onBack = { showMyPosts = false })
                } else if (showSavedPosts) {
                    SavedPostsScreen(api, onBack = { showSavedPosts = false })
                } else if (showMyReports) {
                    MyReportsScreen(api, onBack = { showMyReports = false })
                } else if (viewedProfileUserId != null) {
                    PublicProfileScreen(
                        userId = viewedProfileUserId!!,
                        api = api,
                        onBack = { viewedProfileUserId = null }
                    )
                } else if (showCallHistory) {
                    CallHistoryScreen(callHistory, onBack = { showCallHistory = false })
                } else SocialShell(posts, conversations, friends, notifications, unreadNotificationCount, presenceByUser, profile, api, selectedTab = selectedTab, onTabSelected = { selectedTab = it }, onReloadFeed = { scope.launch { runCatching { posts = api.feed().map { it.toPost() } } } }, onTrustDevice = { scope.launch { runCatching { api.trustCurrentDevice() } } }, onOpenSecurity = { showSecuritySettings = true }, onOpenMyPosts = { showMyPosts = true }, onOpenSavedPosts = { showSavedPosts = true }, onOpenMyReports = { showMyReports = true }, isAdmin = session?.role?.uppercase() in setOf("ADMIN", "ROLE_ADMIN"), onOpenAdmin = { showAdmin = true }, onProfileUpdated = { profile = it }, onFriendsChanged = { accepted ->
                    accepted?.let { friend ->
                        optimisticFriendIds = optimisticFriendIds + friend.id
                        friends = mergeAcceptedFriend(friends, friend)
                    }
                    scope.launch { runCatching { refreshFriendsAndConversations() } }
                }, onNotificationDestination = { notification -> selectedTab = notificationDestinationTab(notification) }, onLogout = {
                    scope.launch { runCatching { api.logout() }; store.clear(); session = null }
                }, onMarkNotificationRead = { notificationId ->
                    scope.launch {
                        runCatching { api.markNotificationRead(notificationId) }.onSuccess {
                            val wasUnread = notifications.any { it.id == notificationId && !it.read }
                            notifications = notifications.map { item -> if (item.id == notificationId) item.copy(read = true) else item }
                            if (wasUnread) unreadNotificationCount = (unreadNotificationCount - 1).coerceAtLeast(0)
                        }
                    }
                }, onMarkNotificationsRead = {
                    scope.launch {
                        runCatching { api.markAllNotificationsRead() }.onSuccess {
                            notifications = notifications.map { it.copy(read = true) }
                            unreadNotificationCount = 0
                        }
                    }
                }, onLoadMoreNotifications = {
                    if (!notificationLoading && notificationPage + 1 < notificationTotalPages) {
                        scope.launch {
                            notificationLoading = true
                            runCatching { api.notifications(notificationPage + 1) }
                                .onSuccess { next ->
                                    notifications = (notifications + next.items).distinctBy { it.id ?: it.hashCode() }
                                    seenNotificationIds.value = seenNotificationIds.value + next.items.mapNotNull { it.id }
                                    notificationPage = next.page
                                    notificationTotalPages = next.totalPages
                                    notificationError = null
                                }
                                .onFailure { notificationError = it.message }
                            notificationLoading = false
                        }
                    }
                }, onRetryNotifications = { scope.launch { syncNotifications(replaceExisting = true) } }, notificationsPage = notificationPage, notificationsTotalPages = notificationTotalPages, notificationsLoading = notificationLoading, notificationsError = notificationError, realtimeConnected = realtimeConnected, realtimeResyncVersion = realtimeResyncVersion, chatMessages = chatMessages, chatEvents = chatEvents, chatTypingByConversation = chatTypingByConversation, onSendChat = { conversationId, content, clientId, replyTo, uploadIds -> stompRef.value?.sendChatMessage(conversationId, content, clientId, replyTo, uploadIds) }, onChatAction = { action, id, content ->
                    when (action) {
                        "DELIVERED" -> stompRef.value?.sendDelivered(id)
                        "SEEN" -> stompRef.value?.sendSeen(id)
                        "EDIT" -> stompRef.value?.sendEdit(id, content.orEmpty())
                        "RECALL" -> stompRef.value?.sendRecall(id)
                        "DELETE" -> stompRef.value?.sendDeleteForMe(id)
                        "REACTION" -> stompRef.value?.sendReaction(id, content ?: "LIKE")
                        "TYPING" -> stompRef.value?.sendTyping(id, content == "true")
                        "TYPING_EXPIRED" -> {
                            val expected = content?.toLongOrNull()
                            if (expected == null || chatTypingByConversation[id] == expected) {
                                chatTypingByConversation = chatTypingByConversation - id
                            }
                        }
                        "CHAT_OPENED" -> {
                            activeChatConversationId = id
                            conversations = conversations.map { conversation ->
                                if (conversation.id == id) conversation.copy(unreadCount = 0) else conversation
                            }
                        }
                        "CHAT_CLOSED" -> {
                            if (activeChatConversationId == id) activeChatConversationId = null
                        }
                        "SEEN_CONVERSATION" -> {
                            val seenAt = java.time.LocalDateTime.now().toString()
                            activeChatConversationId = id
                            chatMessages = markMessagesSeenLocally(chatMessages, id, session?.userId, seenAt)
                            conversations = conversations.map { conversation ->
                                if (conversation.id == id) conversation.copy(unreadCount = 0) else conversation
                            }
                            stompRef.value?.sendSeenConversation(id)
                            scope.launch {
                                var failure: Throwable? = null
                                repeat(2) { attempt ->
                                    val result = runCatching {
                                        api.markConversationSeen(id)
                                        refreshFriendsAndConversations()
                                    }
                                    if (result.isSuccess) return@launch
                                    failure = result.exceptionOrNull()
                                    if (attempt == 0) delay(500)
                                }
                                failure?.let { callError = it.message }
                            }
                        }
                        "RECONNECT" -> session?.token?.let { stompRef.value?.connect(it) }
                    }
                }, onOpenUserProfile = { viewedProfileUserId = it }, storyReactionEvents = storyReactionEvents, onStartCall = { conversation, type -> requestCall(PendingCallRequest(conversation, type)) }, onOpenCallHistory = { showCallHistory = true }, callHistory = callHistory, presenceRefreshToken = presenceRefreshToken)
                loginApprovalPrompts.firstOrNull()?.let { approval ->
                    fun dismissPrompt() {
                        loginApprovalPrompts = loginApprovalPrompts.filterNot { it.id == approval.id }
                        loginApprovalError = null
                    }
                    fun decide(approve: Boolean) {
                        scope.launch {
                            loginApprovalBusy = true
                            loginApprovalError = null
                            runCatching { if (approve) api.approveLogin(approval.id) else api.rejectLogin(approval.id) }
                                .onSuccess { dismissPrompt() }
                                .onFailure { loginApprovalError = it.message ?: "Không thể xử lý yêu cầu đăng nhập" }
                            loginApprovalBusy = false
                        }
                    }
                    AlertDialog(
                        onDismissRequest = { if (!loginApprovalBusy) dismissPrompt() },
                        title = { Text("Có thiết bị đang đăng nhập") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Nếu không phải bạn, hãy từ chối. Bạn vẫn có thể xử lý sau trong Bảo mật đăng nhập.", color = Muted, fontSize = 13.sp)
                                PendingApprovalCard(approval, loginApprovalBusy, onApprove = { decide(true) }, onReject = { decide(false) })
                                loginApprovalError?.let { Text(it, color = Color(0xFFB3261E), fontSize = 12.sp) }
                            }
                        },
                        confirmButton = { TextButton(enabled = !loginApprovalBusy, onClick = { dismissPrompt() }) { Text("Để sau") } }
                    )
                }
                if (showTrustPrompt) {
                    AlertDialog(
                        onDismissRequest = { store.markTrustPromptShown(); showTrustPrompt = false },
                        title = { Text("Tin cậy thiết bị này?") },
                        text = { Text("Bạn có muốn ghi nhớ thiết bị này để giảm yêu cầu xác minh ở những lần đăng nhập sau không?") },
                        confirmButton = { TextButton(onClick = { scope.launch { runCatching { api.trustCurrentDevice() }; store.markTrustPromptShown(); showTrustPrompt = false } }) { Text("Tin cậy") } },
                        dismissButton = { TextButton(onClick = { store.markTrustPromptShown(); showTrustPrompt = false }) { Text("Để sau") } }
                    )
                }
            } else if (pendingApproval != null) {
                ApprovalScreen(
                    approval = pendingApproval!!,
                    api = api,
                    onAuthenticated = { result -> store.save(result); pendingApproval = null; error = null; session = result; showTrustPrompt = !store.isTrustPromptShown() },
                    onBack = { pendingApproval = null; error = null }
                )
            } else {
                LoginScreen(
                    selectedBackendEndpointId = selectedBackendEndpointId,
                    onBackendChanged = ::selectBackendEndpoint,
                    onLogin = { email, password -> scope.launch { runCatching { api.login(email, password) }.onSuccess { result ->
                        if (result.token.isBlank() || !result.loginStatus.equals("SUCCESS", ignoreCase = true)) {
                            if (!result.approvalToken.isNullOrBlank()) pendingApproval = result else error = "Đăng nhập chưa hoàn tất (trạng thái: ${result.loginStatus})."
                        } else { store.save(result); session = result; showTrustPrompt = !store.isTrustPromptShown() }
                    }.onFailure { error = it.message } } },
                    onRegister = { userName, email, password -> scope.launch { runCatching { api.registerAndLogin(userName, email, password) }.onSuccess { result ->
                        if (result.token.isBlank() || !result.loginStatus.equals("SUCCESS", ignoreCase = true)) {
                            if (!result.approvalToken.isNullOrBlank()) pendingApproval = result else error = "Tài khoản đã tạo nhưng đăng nhập chưa hoàn tất (trạng thái: ${result.loginStatus})."
                        } else { store.save(result); session = result; showTrustPrompt = !store.isTrustPromptShown() }
                    }.onFailure { error = it.message } } },
                    error = error
                )
            }
            if (session != null && activeCall?.callId?.isNotBlank() == true) {
                CallOverlay(
                    call = activeCall!!,
                    phase = callPhase,
                    manager = callManager,
                    mediaVersion = callMediaVersion,
                    errorMessage = callError,
                    muted = callMuted,
                    cameraEnabled = callCameraEnabled,
                    frontCamera = callFrontCamera,
                    audioRoute = callAudioRoute,
                    interruptionReason = callInterruptionReason,
                    quality = callQuality,
                    onAccept = {
                        activeCall?.let { call ->
                            requestCall(PendingCallRequest(ApiConversation(call.conversationId, call.callerName, null, "", 0, "DIRECT"), call.callType, acceptingCall = true))
                        }
                    },
                    onReject = { finishCallWithEvent("CALL_REJECT", "/app/call.reject") },
                    onCancel = { finishCallWithEvent("CALL_CANCEL", "/app/call.cancel") },
                    onEnd = { finishCallWithEvent("CALL_END", "/app/call.end") },
                    onToggleMute = { callMuted = callManager.toggleMute() },
                    onToggleCamera = { callCameraEnabled = callManager.toggleCamera() },
                    onSwitchCamera = { callManager.switchCamera() },
                    onToggleAudioRoute = { callAudioRoute = callManager.toggleAudioRoute() }
                )
            }
            }
        }
    }
}

@Composable
private fun ApprovalScreen(
    approval: AuthSession,
    api: ApiClient,
    onAuthenticated: (AuthSession) -> Unit,
    onBack: () -> Unit
) {
    var otp by remember { mutableStateOf("") }
    var trustDevice by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var debugOtp by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    suspend fun requestOtp() {
        val token = approval.approvalToken ?: return
        busy = true
        error = null
        runCatching { api.sendLoginOtp(token) }
            .onSuccess { result ->
                message = result.message
                debugOtp = result.debugOtp
                if (result.debugOtp?.length == 6) otp = result.debugOtp
            }
            .onFailure { error = it.message ?: "Khong the gui OTP" }
        busy = false
    }
    LaunchedEffect(approval.approvalToken) {
        val token = approval.approvalToken ?: return@LaunchedEffect
        requestOtp()
        while (true) {
            delay(2_500L)
            runCatching { api.loginApprovalStatus(token) }
                .onSuccess { status ->
                    if (status.auth?.token?.isNotBlank() == true) {
                        onAuthenticated(status.auth)
                        return@LaunchedEffect
                    }
                    if (!status.status.equals("PENDING", ignoreCase = true)) {
                        error = "YÃªu cáº§u Ä‘Äƒng nháº­p cÃ³ tráº¡ng thÃ¡i: ${status.status}"
                        return@LaunchedEffect
                    }
                }
        }
    }
    fun finish(status: LoginApprovalStatus) {
        if (status.auth?.token?.isNotBlank() == true) onAuthenticated(status.auth)
        else if (status.status != "PENDING") error = "Yêu cầu đăng nhập có trạng thái: ${status.status}"
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp), verticalArrangement = Arrangement.Center) {
        BrandMark()
        Spacer(Modifier.height(26.dp))
        Text("Xác minh đăng nhập", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Thiết bị này cần được xác minh trước khi bạn truy cập tài khoản.", color = Muted, fontSize = 15.sp)
        Spacer(Modifier.height(18.dp))
        Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text("Mã yêu cầu đăng nhập", color = Muted, fontSize = 12.sp)
                Text(approval.approvalToken?.take(12)?.plus("…") ?: "Không xác định", color = Ink, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                Text("Mức độ rủi ro: ${approval.riskScore ?: 0}/100", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(enabled = !busy, onClick = {
            scope.launch { requestOtp() }
        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(Brand)) { Text("Gửi mã OTP qua email") }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(value = otp, onValueChange = { otp = it.filter(Char::isDigit).take(6); error = null }, label = { Text("Mã OTP 6 chữ số") }, singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = trustDevice, onCheckedChange = { trustDevice = it }, enabled = !busy)
            Text("Tin cậy thiết bị sau khi xác minh", color = Ink, fontSize = 13.sp)
        }
        Button(enabled = otp.length == 6 && !busy, onClick = {
            val token = approval.approvalToken ?: return@Button
            scope.launch {
                busy = true; error = null
                runCatching { api.verifyLoginOtp(token, otp, trustDevice) }.onSuccess(::finish).onFailure { error = it.message }
                busy = false
            }
        }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(Brand)) { Text(if (busy) "Đang xác minh..." else "Xác minh OTP") }
        TextButton(enabled = !busy, onClick = {
            val token = approval.approvalToken ?: return@TextButton
            scope.launch {
                busy = true; error = null
                runCatching { api.loginApprovalStatus(token) }.onSuccess(::finish).onFailure { error = it.message }
                busy = false
            }
        }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Tôi đã duyệt trên thiết bị khác", color = Brand) }
        message?.let { Text(it, color = Brand, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) }
        debugOtp?.let { Text("OTP dev: $it", color = Brand, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp)) }
        error?.let { Text(it, color = Color(0xFFB3261E), fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp)) }
        TextButton(onClick = onBack, enabled = !busy, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Quay lại đăng nhập", color = Muted) }
    }
}

@Composable
private fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    error: String?,
    selectedBackendEndpointId: String = LOCAL_BACKEND_ENDPOINT.id,
    onBackendChanged: (BackendEndpoint) -> Unit = {}
) {
    var isRegistering by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    val visibleError = validationError ?: error

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        BrandMark()
        Spacer(Modifier.height(22.dp))
        Text(if (isRegistering) "Tạo tài khoản" else "Chào mừng trở lại", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(8.dp))
        Text("Kết nối với bạn bè và những câu chuyện bạn quan tâm.", color = Muted, fontSize = 15.sp)
        Spacer(Modifier.height(28.dp))
        Text("Máy chủ kết nối", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Column(Modifier.padding(vertical = 4.dp)) {
                BACKEND_ENDPOINTS.forEach { endpoint ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onBackendChanged(endpoint) }.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = endpoint.id == selectedBackendEndpointId,
                            onClick = { onBackendChanged(endpoint) }
                        )
                        Column(Modifier.padding(start = 4.dp)) {
                            Text(endpoint.label, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(endpoint.baseUrl, color = Muted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        if (isRegistering) {
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tên người dùng") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(Modifier.height(12.dp))
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Mật khẩu") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(16.dp)
        )
        if (!isRegistering) TextButton(onClick = {}, modifier = Modifier.align(Alignment.End)) {
            Text("Quên mật khẩu?", color = Brand)
        }
        Button(
            onClick = {
                val normalizedEmail = email.trim()
                val normalizedPassword = password.trim()
                validationError = when {
                    isRegistering && userName.trim().isBlank() -> "Vui lòng nhập tên người dùng"
                    normalizedEmail.isBlank() -> "Vui lòng nhập email"
                    normalizedPassword.isBlank() -> "Vui lòng nhập mật khẩu"
                    isRegistering && normalizedPassword.length < 6 -> "Mật khẩu phải có ít nhất 6 ký tự"
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches() -> "Email không hợp lệ"
                    else -> null
                }
                if (validationError == null) {
                    if (isRegistering) onRegister(userName.trim(), normalizedEmail, normalizedPassword)
                    else onLogin(normalizedEmail, normalizedPassword)
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Brand)
        ) { Text(if (isRegistering) "Đăng ký" else "Đăng nhập", fontWeight = FontWeight.Bold) }
        visibleError?.takeIf { it.isNotBlank() }?.let { message ->
            Text(message, color = Color(0xFFB3261E), fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Divider(Modifier.weight(1f), color = Color(0xFFDDE5EA))
            Text("  hoặc  ", color = Muted, fontSize = 12.sp)
            Divider(Modifier.weight(1f), color = Color(0xFFDDE5EA))
        }
        Spacer(Modifier.height(18.dp))
        OutlinedButton(
            onClick = { isRegistering = !isRegistering; validationError = null },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) { Text(if (isRegistering) "Đã có tài khoản? Đăng nhập" else "Tạo tài khoản mới", color = Brand, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun BrandMark() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Brand),
            contentAlignment = Alignment.Center
        ) { Text("S", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black) }
        Spacer(Modifier.width(12.dp))
        Text("social", color = Brand, fontSize = 25.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun SocialShell(posts: List<Post>, conversations: List<ApiConversation>, friends: List<ApiFriend>, notifications: List<ApiNotification>, unreadNotificationCount: Int, presenceByUser: Map<Long, ApiPresence>, profile: ApiProfile?, api: ApiClient, selectedTab: Int, onTabSelected: (Int) -> Unit, onReloadFeed: () -> Unit, onTrustDevice: () -> Unit, onOpenSecurity: () -> Unit, onOpenMyPosts: () -> Unit, onOpenSavedPosts: () -> Unit, onOpenMyReports: () -> Unit, isAdmin: Boolean, onOpenAdmin: () -> Unit, onProfileUpdated: (ApiProfile) -> Unit, onFriendsChanged: (ApiFriend?) -> Unit = {}, onNotificationDestination: (ApiNotification) -> Unit, onLogout: () -> Unit, onMarkNotificationRead: (Long) -> Unit, onMarkNotificationsRead: () -> Unit, onLoadMoreNotifications: () -> Unit, onRetryNotifications: () -> Unit = {}, notificationsPage: Int = 0, notificationsTotalPages: Int = 0, notificationsLoading: Boolean = false, notificationsError: String? = null, realtimeConnected: Boolean = false, realtimeResyncVersion: Long = 0L, chatMessages: List<ApiMessage> = emptyList(), chatEvents: List<ChatRealtimeEvent> = emptyList(), chatTypingByConversation: Map<Long, Long> = emptyMap(), onSendChat: (Long, String, String, Long?, List<Long>) -> Unit = { _, _, _, _, _ -> }, onChatAction: (String, Long, String?) -> Unit = { _, _, _ -> }, onOpenUserProfile: (Long) -> Unit = {}, storyReactionEvents: List<StoryReactionEvent> = emptyList(), onStartCall: (ApiConversation, String) -> Unit = { _, _ -> }, onOpenCallHistory: () -> Unit = {}, callHistory: List<ApiCallHistory> = emptyList(), @Suppress("UNUSED_PARAMETER") presenceRefreshToken: Long = 0L) {
    var showCreatePost by remember { mutableStateOf(false) }
    var storyViewer by remember { mutableStateOf<StoryViewerSelection?>(null) }
    LaunchedEffect(selectedTab) { if (selectedTab != 0) VideoPlaybackManager.pauseAll() }
    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Background,
            topBar = { SocialTopHeader(selectedTab, profile, conversations.sumOf { it.unreadCount }, unreadNotificationCount, onTabSelected, onCreatePost = { showCreatePost = true }) }
        ) { padding ->
            when (selectedTab) {
                0 -> FeedScreen(posts, api = api, onReload = onReloadFeed, storyReactionEvents = storyReactionEvents, onOpenStory = { users, userIndex -> storyViewer = StoryViewerSelection(users, userIndex) }, modifier = Modifier.padding(padding))
                1 -> VideoTabScreen(api, Modifier.padding(padding))
                2 -> FriendsScreen(friends, presenceByUser, api, onOpenUserProfile, onFriendsChanged, Modifier.padding(padding))
                3 -> ChatScreen(friends, conversations, presenceByUser, callHistory, api, chatMessages, chatEvents, chatTypingByConversation, realtimeConnected, realtimeResyncVersion, onSendChat, onChatAction, onStartCall, onOpenCallHistory, Modifier.padding(padding))
                4 -> NotificationsScreen(notifications, onMarkNotificationsRead, onMarkNotificationRead, onNotificationDestination, onLoadMoreNotifications, onRetryNotifications, notificationsPage, notificationsTotalPages, notificationsLoading, notificationsError, realtimeConnected, Modifier.padding(padding))
                else -> ProfileScreen(profile, api, onProfileUpdated, onTrustDevice, onOpenSecurity, onOpenMyPosts, onOpenSavedPosts, onOpenMyReports, isAdmin, onOpenAdmin, onLogout, Modifier.padding(padding))
            }
        }
        storyViewer?.let { selection ->
            StoryViewerScreen(
                selection = selection,
                api = api,
                onClose = { storyViewer = null }
            )
        }
    }
    if (showCreatePost) PostComposerDialog(api, onDismiss = { showCreatePost = false }, onCreated = { showCreatePost = false; onReloadFeed() })
}

@Composable
private fun SocialTopHeader(selectedTab: Int, profile: ApiProfile?, unreadChat: Int, unreadNotifications: Int, onTabSelected: (Int) -> Unit, onCreatePost: () -> Unit) {
    val tabs = listOf("Trang chủ" to Icons.Filled.Home, "Video" to Icons.Filled.VideoLibrary, "Bạn bè" to Icons.Filled.Person, "Chat" to Icons.Filled.ChatBubbleOutline, "Thông báo" to Icons.Filled.Notifications, "Cá nhân" to Icons.Filled.Person)
    Column(Modifier.fillMaxWidth().background(Color.White).shadow(2.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) { Icon(Icons.Filled.MoreHoriz, "Menu", tint = Ink) }
            Text("social", color = Brand, fontSize = 28.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            IconButton(onClick = onCreatePost) { Icon(Icons.Filled.AddBox, "Tạo mới", tint = Ink) }
            IconButton(onClick = { onTabSelected(2) }) { Icon(Icons.Filled.Search, "Tìm kiếm", tint = Ink) }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 3.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            tabs.forEachIndexed { index, (label, icon) ->
                Box(Modifier.weight(1f).clickable { onTabSelected(index) }.padding(top = 3.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box {
                            if (index == 5 && profile != null) RemoteAvatar(profile.userName, profile.avatarUrl, SoftBrand, 23.dp)
                            else Icon(icon, label, tint = if (selectedTab == index) Brand else Muted, modifier = Modifier.size(23.dp))
                            val badge = when (index) { 3 -> unreadChat; 4 -> unreadNotifications; else -> 0 }
                            if (badge > 0) Box(Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-5).dp).size(16.dp).clip(CircleShape).background(Color(0xFFD93025)), contentAlignment = Alignment.Center) { Text(if (badge > 99) "99+" else badge.toString(), color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold) }
                        }
                        Spacer(Modifier.height(4.dp))
                        Box(Modifier.fillMaxWidth().height(3.dp).background(if (selectedTab == index) Brand else Color.Transparent))
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoTabScreen(api: ApiClient, modifier: Modifier = Modifier) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    LaunchedEffect(Unit) { runCatching { posts = api.videoFeed().map { it.toPost() } } }
    ReelsScreen(posts, api, modifier = modifier)
}

@Composable
private fun FeedScreen(posts: List<Post>, api: ApiClient, onReload: () -> Unit, storyReactionEvents: List<StoryReactionEvent> = emptyList(), onOpenStory: (List<ApiStoryUser>, Int) -> Unit, modifier: Modifier = Modifier) {
    var visiblePosts by remember { mutableStateOf(posts) }
    var showComposer by remember { mutableStateOf(false) }
    val feedListState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(posts) { visiblePosts = posts }
    val activeInfo = feedListState.layoutInfo.visibleItemsInfo.filter { it.index > 0 }.maxByOrNull { info ->
        val top = maxOf(info.offset, feedListState.layoutInfo.viewportStartOffset)
        val bottom = minOf(info.offset + info.size, feedListState.layoutInfo.viewportEndOffset)
        maxOf(0, bottom - top)
    }
    val activeVisiblePixels = activeInfo?.let { info ->
        val top = maxOf(info.offset, feedListState.layoutInfo.viewportStartOffset)
        val bottom = minOf(info.offset + info.size, feedListState.layoutInfo.viewportEndOffset)
        maxOf(0, bottom - top)
    } ?: 0
    val activeFeedId = if (activeInfo != null && activeVisiblePixels >= activeInfo.size * 0.6f) visiblePosts.getOrNull(activeInfo.index - 1)?.id else null
    LazyColumn(state = feedListState, modifier = modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 20.dp)) {
        item {
            ComposerCard(onClick = { showComposer = true })
            StoriesRow(api, storyReactionEvents, onOpenStory)
        }
        if (visiblePosts.isEmpty()) item { Text("Chưa có bài viết phù hợp", color = Muted, modifier = Modifier.padding(24.dp)) }
        else items(visiblePosts, key = { it.id ?: it.hashCode().toLong() }) { post -> PostCard(post, api, active = post.id != null && post.id == activeFeedId, onChanged = onReload) }
    }
    if (showComposer) PostComposerDialog(api, onDismiss = { showComposer = false }, onCreated = { showComposer = false; onReload() })
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
private fun ReelsScreen(posts: List<Post>, api: ApiClient, modifier: Modifier = Modifier) {
    var activeIndex by remember { mutableStateOf(0) }
    var muted by remember { mutableStateOf(true) }
    var commentPost by remember { mutableStateOf<Post?>(null) }
    var reactionPost by remember { mutableStateOf<Post?>(null) }
    val state = androidx.compose.foundation.lazy.rememberLazyListState()
    val reels = posts.ifEmpty { listOf(Post(null, null, "Social", null, "", "Chua co video tu backend", emptyList(), 0, 0, 0, null)) }
    LaunchedEffect(state.firstVisibleItemIndex) { activeIndex = state.firstVisibleItemIndex.coerceIn(0, reels.lastIndex) }
    Box(modifier.fillMaxSize().background(Color.Black)) {
        LazyColumn(state = state, modifier = Modifier.fillMaxSize(), flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(state)) {
            items(reels, key = { it.id ?: it.hashCode().toLong() }) { post ->
                ReelItem(post = post, active = reels.indexOf(post) == activeIndex, muted = muted, api = api, onMute = { muted = !muted }, onComment = { commentPost = post }, onReactionPicker = { reactionPost = post })
            }
        }
    }
    reactionPost?.let { post -> ReelReactionPicker(post, api, onDismiss = { reactionPost = null }) }
    commentPost?.let { post -> ReelCommentSheet(post, api, muted, onMute = { muted = !muted }, onDismiss = { commentPost = null }) }
}

@Composable
private fun ReelItem(post: Post, active: Boolean, muted: Boolean, api: ApiClient, onMute: () -> Unit, onComment: () -> Unit, onReactionPicker: () -> Unit) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val scope = rememberCoroutineScope()
    var liked by remember(post.id) { mutableStateOf(post.myReaction != null) }
    var reactions by remember(post.id) { mutableStateOf(post.totalReactions) }
    var shares by remember(post.id) { mutableStateOf(post.shareCount) }
    var saved by remember(post.id) { mutableStateOf(false) }
    var expanded by remember(post.id) { mutableStateOf(false) }
    val media = post.media.firstOrNull { it.type.equals("VIDEO", true) && !it.url.isNullOrBlank() }
    Box(Modifier.fillMaxWidth().height(configuration.screenHeightDp.dp).background(Color.Black)) {
        if (active && media?.url != null) {
            ReelVideoPlayer(media.url, muted, Modifier.fillMaxSize(), videoId = "reel:${post.id ?: post.name}:$media")
        } else if (media?.url != null) {
            Box(Modifier.fillMaxSize().background(Color(0xFF090909)))
        }
        Box(Modifier.fillMaxSize().background(Color.Transparent))
        Column(Modifier.align(Alignment.CenterEnd).padding(end = 12.dp, top = 110.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            ReelAction(Icons.Filled.ThumbUp, compactCount(reactions), liked, onClick = { val id = post.id ?: return@ReelAction; scope.launch { runCatching { if (liked) { api.removeReaction(id); reactions = (reactions - 1).coerceAtLeast(0) } else { api.react(id, "LIKE"); reactions += 1 }; liked = !liked } } }, onLongClick = onReactionPicker)
            ReelAction(Icons.Filled.ChatBubbleOutline, compactCount(post.commentCount), false, onClick = onComment)
            ReelAction(Icons.Filled.Share, compactCount(shares), false, onClick = { post.id?.let { id -> scope.launch { runCatching { api.sharePost(id); shares += 1 } } } })
            ReelAction(Icons.Filled.Bookmark, if (saved) "Da luu" else "Luu", saved, onClick = { post.id?.let { id -> scope.launch { runCatching { if (saved) api.unsavePost(id) else api.savePost(id); saved = !saved } } } })
            IconButton(onClick = {}) { Icon(Icons.Filled.MoreHoriz, "Them", tint = Color.White, modifier = Modifier.size(28.dp)) }
        }
        Column(Modifier.align(Alignment.BottomStart).padding(start = 18.dp, end = 74.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { RemoteAvatar(post.name, post.avatarUrl, Color.DarkGray, 42.dp); Spacer(Modifier.width(9.dp)); Text(post.name, color = Color.White, fontWeight = FontWeight.Bold); Spacer(Modifier.width(10.dp)); OutlinedButton(onClick = {}, shape = RoundedCornerShape(18.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 0.dp), colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("Theo doi", fontSize = 12.sp) } }
            Text(post.content, color = Color.White, maxLines = if (expanded) 5 else 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.clickable { expanded = !expanded })
            Text("Moi danh cho ban  •  ${formatPostTime(post.time)}", color = Color.White.copy(alpha = .75f), fontSize = 12.sp)
        }
        IconButton(onClick = onMute, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 164.dp).clip(CircleShape).background(Color.Black.copy(alpha = .55f))) { Icon(if (muted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp, "Am thanh", tint = Color.White) }
    }
}

@Composable
private fun ReelVideoPlayer(url: String, muted: Boolean, modifier: Modifier, videoId: String) {
    var playing by remember(url) { mutableStateOf(true) }
    var playerView by remember(url) { mutableStateOf<VideoView?>(null) }
    var mediaPlayer by remember(url) { mutableStateOf<MediaPlayer?>(null) }
    AndroidView(modifier = modifier.clickable { playing = !playing }, factory = { context -> VideoView(context).apply { playerView = this; VideoPlaybackManager.register(videoId, this); setVideoURI(Uri.parse(url)); setOnPreparedListener { player -> mediaPlayer = player; player.isLooping = true; player.setVolume(if (muted) 0f else 1f, if (muted) 0f else 1f); if (playing) VideoPlaybackManager.play(videoId) } } }, update = { view -> VideoPlaybackManager.register(videoId, view); if (playing) VideoPlaybackManager.play(videoId) else VideoPlaybackManager.pause(videoId) })
    LaunchedEffect(muted) { mediaPlayer?.setVolume(if (muted) 0f else 1f, if (muted) 0f else 1f) }
    DisposableEffect(videoId) { onDispose { VideoPlaybackManager.unregister(videoId, playerView ?: return@onDispose); playerView?.stopPlayback(); playerView = null; mediaPlayer = null } }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReelAction(icon: androidx.compose.ui.graphics.vector.ImageVector, count: String, selected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)) { Icon(icon, null, tint = if (selected) Brand else Color.White, modifier = Modifier.size(32.dp)); Text(count, color = Color.White, fontSize = 12.sp) }
}

private fun compactCount(value: Int): String = when { value >= 1000 -> String.format(java.util.Locale.US, "%.1fK", value / 1000f).replace(".0K", "K").replace('.', ','); else -> value.toString() }

@Composable
private fun ReelReactionPicker(post: Post, api: ApiClient, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    // Keep the picker exactly aligned with backend ReactionType.
    val choices = listOf("ANGRY" to "😡", "SAD" to "😢", "WOW" to "😮", "HAHA" to "😂", "LOVE" to "❤️", "LIKE" to "👍")
    Box(Modifier.fillMaxSize().clickable(onClick = onDismiss), contentAlignment = Alignment.CenterEnd) {
        Surface(color = Color(0xEE252525), shape = RoundedCornerShape(28.dp), shadowElevation = 8.dp, modifier = Modifier.padding(end = 58.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.padding(horizontal = 9.dp, vertical = 7.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) { choices.forEach { (type, emoji) -> Text(emoji, fontSize = 27.sp, modifier = Modifier.clickable { post.id?.let { id -> scope.launch { runCatching { api.react(id, type) }; onDismiss() } } }.padding(2.dp)) } }
                ReactionCountSummary(post.reactionCounts, post.totalReactions)
                Spacer(Modifier.height(5.dp))
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ReelCommentSheet(post: Post, api: ApiClient, muted: Boolean, onMute: () -> Unit, onDismiss: () -> Unit) {
    var comments by remember { mutableStateOf<List<ApiComment>>(emptyList()) }
    var page by remember { mutableStateOf(0) }
    var totalPages by remember { mutableStateOf(0) }
    var totalItems by remember { mutableStateOf(post.commentCount.toLong()) }
    var loading by remember { mutableStateOf(false) }
    var loadingMore by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var draft by remember { mutableStateOf("") }
    var showManager by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    suspend fun reload(firstPage: Boolean = true) {
        val id = post.id ?: return
        if (firstPage) loading = true else loadingMore = true
        error = null
        runCatching { api.comments(id, if (firstPage) 0 else page + 1) }
            .onSuccess { loaded ->
                comments = if (firstPage) loaded.items else mergeComments(comments, loaded.items)
                page = loaded.page
                totalPages = loaded.totalPages
                totalItems = loaded.totalItems
            }
            .onFailure { error = it.message ?: "Không thể tải bình luận" }
        if (firstPage) loading = false else loadingMore = false
    }
    LaunchedEffect(post.id) { reload() }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(Modifier.fillMaxWidth().heightIn(min = 460.dp, max = 680.dp).padding(horizontal = 18.dp)) {
            Box(Modifier.width(42.dp).height(4.dp).clip(RoundedCornerShape(4.dp)).background(Color.Gray).align(Alignment.CenterHorizontally))
            Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Phù hợp nhất ▾  ·  $totalItems bình luận", color = Ink, fontSize = 17.sp); IconButton(onClick = onMute) { Icon(if (muted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp, "Âm thanh", tint = Ink) } }
            error?.let { Text(it, color = Color(0xFFB3261E), fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp)) }
            if (loading) {
                Text("Đang tải bình luận...", color = Muted, modifier = Modifier.padding(vertical = 24.dp))
            } else if (comments.isEmpty()) {
                Text("Chưa có bình luận.", color = Muted, modifier = Modifier.padding(vertical = 24.dp))
            } else {
                LazyColumn(Modifier.weight(1f)) {
                    comments.filter { it.parentCommentId == null }.forEach { root ->
                        item(key = "root-${root.id}") { CommentTreeItem(root, comments, onReply = { showManager = true }) }
                    }
                    if (page + 1 < totalPages) item {
                        TextButton(enabled = !loadingMore, onClick = { scope.launch { reload(false) } }) { Text(if (loadingMore) "Đang tải..." else "Xem thêm bình luận", color = Brand) }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Avatar("T", SoftBrand, 36.dp); Spacer(Modifier.width(8.dp)); OutlinedTextField(draft, { draft = it.take(2000) }, placeholder = { Text("Bình luận dưới tên của bạn") }, enabled = !loading, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(22.dp)); TextButton(enabled = draft.isNotBlank() && !loading, onClick = { post.id?.let { id -> scope.launch { loading = true; error = null; runCatching { api.comment(id, draft.trim()) }.onSuccess { created -> comments = mergeComments(comments, listOf(created)); totalItems += 1; draft = "" }.onFailure { error = it.message ?: "Không thể gửi bình luận" }; loading = false } } }) { Text("Gửi", color = Brand) } }
        }
    }
    if (showManager) CommentManagerDialog(
        api = api,
        initial = comments,
        onDismiss = { showManager = false },
        onReload = { post.id?.let { api.comments(it) } ?: ApiCommentPage(emptyList(), 0, 20, 0, 0) },
        onChanged = { updated, loaded ->
            comments = updated
            loaded?.let {
                page = it.page
                totalPages = it.totalPages
                totalItems = it.totalItems
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ShareChoiceSheet(
    onDismiss: () -> Unit,
    onShareNow: () -> Unit,
    onShareWithContent: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(Modifier.width(38.dp).height(4.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF98A2B3)).align(Alignment.CenterHorizontally))
            Text("Chia sẻ bài viết", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            TextButton(onClick = onShareNow, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Share, null, tint = Brand)
                Spacer(Modifier.width(10.dp))
                Text("Chia sẻ ngay", color = Ink, modifier = Modifier.weight(1f))
            }
            TextButton(onClick = onShareWithContent, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Edit, null, tint = Brand)
                Spacer(Modifier.width(10.dp))
                Text("Chia sẻ có nội dung", color = Ink, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SharePostDialog(api: ApiClient, postId: Long?, onDismiss: () -> Unit, onShared: (ApiShareResult) -> Unit) {
    var content by remember { mutableStateOf("") }
    var privacy by remember { mutableStateOf("PUBLIC") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    ModalBottomSheet(onDismissRequest = { if (!busy) onDismiss() }, containerColor = Color(0xFFF7F8FA)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.width(38.dp).height(4.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF98A2B3)).align(Alignment.CenterHorizontally))
            Text("Chia sẻ bài viết", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Surface(Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Avatar("M", SoftBrand, 42.dp); Spacer(Modifier.width(10.dp)); Text("Tài khoản của bạn", color = Ink, fontWeight = FontWeight.Bold) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("PUBLIC" to "Bảng feed", "FRIENDS" to "Bạn bè").forEach { (value, label) ->
                            TextButton(onClick = { privacy = value }, modifier = Modifier.background(if (privacy == value) SoftBrand else Color(0xFFF0F2F5), RoundedCornerShape(18.dp))) { Text(label, color = if (privacy == value) Brand else Muted) }
                        }
                    }
                    OutlinedTextField(content, { content = it.take(5000) }, placeholder = { Text("Bạn nói gì đi…") }, minLines = 3, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("🙂   @ Gắn thẻ", color = Muted, fontSize = 13.sp)
                        Button(enabled = postId != null && !busy, onClick = { scope.launch { busy = true; runCatching { api.sharePost(postId!!, content.trim(), privacy) }.onSuccess { onShared(it) }.onFailure { error = it.message ?: "Không thể chia sẻ" }; busy = false } }, colors = ButtonDefaults.buttonColors(Brand), shape = RoundedCornerShape(8.dp)) { Text(if (busy) "Đang chia sẻ..." else "Chia sẻ ngay") }
                    }
                    error?.let { Text(it, color = Color(0xFFB3261E), fontSize = 12.sp) }
                }
            }
            Text("Chia sẻ nhanh", color = Ink, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                listOf("Tin của bạn" to Icons.Filled.Add, "Tin nhắn" to Icons.Filled.ChatBubble, "Sao chép liên kết" to Icons.Filled.Share).forEach { (label, icon) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(76.dp)) { Box(Modifier.size(54.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) { Icon(icon, label, tint = Brand) }; Text(label, color = Muted, fontSize = 11.sp, maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun PostComposerDialog(api: ApiClient, onDismiss: () -> Unit, onCreated: () -> Unit) {
    var content by remember { mutableStateOf("") }
    var privacy by remember { mutableStateOf("PUBLIC") }
    var busy by remember { mutableStateOf(false) }
    var uploading by remember { mutableStateOf(false) }
    var pendingUploads by remember { mutableStateOf<List<PendingUpload>>(emptyList()) }
    var uploadStatus by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) scope.launch {
            val remaining = 10 - pendingUploads.size
            if (remaining <= 0) {
                errorMessage = "Bai viet toi da 10 media"
                return@launch
            }
            val selected = uris.take(remaining)
            errorMessage = if (uris.size > remaining) "Chi co the them $remaining media nua" else null
            uploading = true
            selected.forEachIndexed { index, uri ->
                uploadStatus = "Dang tai ${index + 1}/${selected.size}"
                runCatching {
                    val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
                    val bytes = readValidatedUpload(context, uri, mime)
                    api.uploadMedia("post-${System.currentTimeMillis()}-$index", mime, bytes)
                }.onSuccess { upload ->
                    val id = requireNotNull(upload.uploadId) { "Backend khong tra ve ma tep" }
                    pendingUploads = pendingUploads + PendingUpload(
                        uploadId = id,
                        fileName = upload.originalFileName ?: "Tep ${index + 1}",
                        type = upload.type ?: "FILE",
                        url = upload.url,
                        contentType = upload.contentType,
                        fileSize = upload.fileSize
                    )
                }.onFailure { cause ->
                    errorMessage = cause.message ?: "Khong the tai tep len"
                }
            }
            uploading = false
            uploadStatus = null
        }
    }

    AlertDialog(
        onDismissRequest = { if (!busy && !uploading) onDismiss() },
        title = { Text("Tao bai viet") },
        text = {
            Column {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it.take(5000) },
                    label = { Text("Ban dang nghi gi?") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { picker.launch("*/*") },
                    enabled = !busy && !uploading && pendingUploads.size < 10
                ) {
                    Icon(Icons.Filled.Add, null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (uploading) uploadStatus ?: "Dang tai tep..." else "Them anh, video hoac tep")
                }
                if (pendingUploads.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pendingUploads.forEach { upload ->
                            PendingUploadChip(upload) {
                                pendingUploads = pendingUploads.filterNot { it.uploadId == upload.uploadId }
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("PUBLIC", "FRIENDS").forEach { value ->
                        OutlinedButton(onClick = { privacy = value }) {
                            Text(if (value == "PUBLIC") "Cong khai" else "Ban be", color = if (privacy == value) Brand else Muted)
                        }
                    }
                }
                errorMessage?.let { Text(it, color = Color(0xFFB3261E), fontSize = 12.sp) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = (content.isNotBlank() || pendingUploads.isNotEmpty()) && !busy && !uploading,
                onClick = {
                    scope.launch {
                        busy = true
                        errorMessage = null
                        runCatching {
                            api.createPost(content.trim(), privacy, mediaIds = pendingUploads.map { it.uploadId })
                        }.onSuccess { onCreated() }
                            .onFailure { errorMessage = it.message ?: "Khong the tao bai viet" }
                        busy = false
                    }
                }
            ) { Text(if (busy) "Dang dang..." else "Dang", color = Brand) }
        },
        dismissButton = { TextButton(enabled = !busy && !uploading, onClick = onDismiss) { Text("Huy") } }
    )
}

@Composable
private fun AppHeader(title: String, action: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = Ink, fontSize = 25.sp, fontWeight = FontWeight.Black)
        action()
    }
}

@Composable
private fun StoriesRow(api: ApiClient, storyReactionEvents: List<StoryReactionEvent> = emptyList(), onOpenStory: (List<ApiStoryUser>, Int) -> Unit) {
    var users by remember { mutableStateOf<List<ApiStoryUser>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showCreate by remember { mutableStateOf(false) }
    var showMine by remember { mutableStateOf(false) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    LaunchedEffect(Unit) { runCatching { api.stories() }.onSuccess { users = it }.also { loading = false } }
    // Events from before this row was composed are already reflected in the freshly loaded feed.
    var appliedReactionNonce by remember { mutableStateOf(storyReactionEvents.lastOrNull()?.nonce ?: 0L) }
    LaunchedEffect(storyReactionEvents.lastOrNull()?.nonce) {
        val fresh = storyReactionEvents.newerThan(appliedReactionNonce)
        if (fresh.isEmpty()) return@LaunchedEffect
        appliedReactionNonce = fresh.last().nonce
        users = users.map { user -> user.copy(stories = user.stories.map { it.withReactionEvents(fresh) }) }
    }
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StoryCard("Tạo tin", null, true, false, onClick = { showCreate = true })
        if (loading) Box(Modifier.width(124.dp).height(220.dp), contentAlignment = Alignment.Center) { Text("Đang tải...", color = Muted) }
        users.forEach { user ->
            val story = user.stories.firstOrNull()
            StoryCard(user.authorName, story?.media?.url, false, user.hasUnseenStory, user.avatarUrl) {
                if (user.stories.isNotEmpty()) {
                    val availableUsers = users.filter { it.stories.isNotEmpty() }
                    onOpenStory(availableUsers, availableUsers.indexOfFirst { it.authorId == user.authorId }.coerceAtLeast(0))
                }
            }
        }
        if (!loading && users.isEmpty()) Text("Chưa có story", color = Muted, modifier = Modifier.padding(vertical = 90.dp))
    }
    TextButton(onClick = { showMine = true }, modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)) { Text("Quản lý story của tôi", color = Brand, fontSize = 12.sp) }
    if (showCreate) StoryComposer(api, onDismiss = { showCreate = false }, onCreated = { showCreate = false; scope.launch { users = api.stories() } })
    if (showMine) MyStoriesDialog(api, onDismiss = { showMine = false })
}

@Composable
private fun StoryCard(name: String, mediaUrl: String?, isCreate: Boolean, unseen: Boolean, avatarUrl: String? = null, onClick: () -> Unit) {
    Box(Modifier.width(124.dp).height(220.dp).clip(RoundedCornerShape(12.dp)).background(if (isCreate) Color.White else Color(0xFF374151)).clickable(onClick = onClick)) {
        if (mediaUrl != null) RemoteImage(mediaUrl, Modifier.fillMaxSize())
        if (!isCreate) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.18f)))
            Box(Modifier.padding(8.dp).size(36.dp).border(2.dp, if (unseen) Brand else Color.White, CircleShape)) { RemoteAvatar(name, avatarUrl, SoftBrand, 32.dp) }
            Text(name, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.align(Alignment.BottomStart).padding(10.dp))
        } else {
            Box(Modifier.fillMaxWidth().height(136.dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)).background(SoftBrand), contentAlignment = Alignment.Center) { Avatar("M", SoftBrand, 62.dp) }
            Box(Modifier.align(Alignment.TopCenter).padding(top = 118.dp).size(34.dp).clip(CircleShape).background(Brand), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Add, "Tạo tin", tint = Color.White) }
            Text(name, color = Ink, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp))
        }
    }
}

@Composable
private fun StoryViewerDialog(story: ApiStory, api: ApiClient, onPrevious: () -> Unit, onNext: () -> Unit, hasPrevious: Boolean, hasNext: Boolean, onDismiss: () -> Unit) {
    var reaction by remember(story.id) { mutableStateOf(story.myReaction) }
    var reactionCount by remember(story.id) { mutableStateOf(story.reactionCount) }
    var showReactionPicker by remember(story.id) { mutableStateOf(false) }
    var reply by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var viewers by remember { mutableStateOf<List<ApiStoryViewer>?>(null) }
    var musicPlaying by remember(story.id) { mutableStateOf(false) }
    var musicMuted by remember(story.id) { mutableStateOf(false) }
    var musicToggle by remember(story.id) { mutableStateOf<(() -> Unit)?>(null) }
    var musicSetMuted by remember(story.id) { mutableStateOf<((Boolean) -> Unit)?>(null) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(story.id) { story.id?.let { runCatching { api.viewStory(it) } } }
    // Realtime reactions only reach the author, who cannot react to their own story, so the
    // incoming count never races with the optimistic updates below.
    LaunchedEffect(story.id, story.reactionCount) {
        if (reactionCount == story.reactionCount) return@LaunchedEffect
        reactionCount = story.reactionCount
        if (viewers != null) story.id?.let { id -> runCatching { api.storyViewers(id) }.onSuccess { viewers = it } }
    }
    DisposableEffect(story.id, story.music?.audioUrl, story.music?.startMs, story.music?.durationMs, lifecycleOwner) {
        val music = story.music
        val handler = Handler(Looper.getMainLooper())
        val playbackState = StoryMusicPlaybackState()
        var player: MediaPlayer? = null
        var loopTask: Runnable? = null
        var disposed = false

        fun scheduleLoop() {
            loopTask?.let(handler::removeCallbacks)
            val currentPlayer = player ?: return
            val task = object : Runnable {
                override fun run() {
                    if (disposed) return
                    runCatching {
                        if (!currentPlayer.isPlaying) return@runCatching
                        val start = music?.startMs?.coerceAtLeast(0L) ?: 0L
                        currentPlayer.pause()
                        currentPlayer.seekTo(start.coerceAtMost((currentPlayer.duration - 1).coerceAtLeast(0).toLong()).toInt())
                        currentPlayer.start()
                        handler.postDelayed(this, music?.durationMs?.coerceAtLeast(1000L) ?: 1000L)
                    }.onFailure { musicPlaying = false }
                }
            }
            loopTask = task
            handler.postDelayed(task, music?.durationMs?.coerceAtLeast(1000L) ?: 1000L)
        }

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    val playing = runCatching { player?.isPlaying == true }.getOrDefault(false)
                    playbackState.onStop(playing)
                    if (playing) runCatching { player?.pause() }
                    musicPlaying = false
                    loopTask?.let(handler::removeCallbacks)
                }
                Lifecycle.Event.ON_START -> {
                    if (playbackState.onStart()) {
                        player?.let { current ->
                            runCatching {
                                val start = music?.startMs?.coerceAtLeast(0L) ?: 0L
                                current.seekTo(start.coerceAtMost((current.duration - 1).coerceAtLeast(0).toLong()).toInt())
                                current.start()
                                musicPlaying = true
                                scheduleLoop()
                            }
                        }
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        if (music != null && music.audioUrl.isNotBlank()) {
            runCatching {
                player = MediaPlayer().apply {
                    setDataSource(music.audioUrl)
                    setVolume(music.volume.toFloat().coerceIn(0f, 1f), music.volume.toFloat().coerceIn(0f, 1f))
                    setOnPreparedListener { prepared ->
                        val start = music.startMs.coerceAtLeast(0L)
                        prepared.seekTo(start.coerceAtMost((prepared.duration - 1).coerceAtLeast(0).toLong()).toInt())
                        if (playbackState.onPrepared()) {
                            prepared.start()
                            musicPlaying = true
                            scheduleLoop()
                        }
                    }
                    setOnErrorListener { _, _, _ -> true }
                    prepareAsync()
                }
                musicToggle = {
                    val current = player
                    if (current != null) {
                        runCatching {
                            if (current.isPlaying) {
                                current.pause()
                                playbackState.onManualPause()
                                musicPlaying = false
                                loopTask?.let(handler::removeCallbacks)
                            } else {
                                if (playbackState.onManualStart()) {
                                    current.start()
                                    musicPlaying = true
                                    scheduleLoop()
                                } else Unit
                            }
                        }
                    }
                }
                musicSetMuted = { muted ->
                    runCatching { player?.setVolume(if (muted) 0f else music.volume.toFloat().coerceIn(0f, 1f), if (muted) 0f else music.volume.toFloat().coerceIn(0f, 1f)) }
                }
            }
        }
        onDispose {
            disposed = true
            playbackState.dispose()
            musicPlaying = false
            musicToggle = null
            musicSetMuted = null
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            loopTask?.let(handler::removeCallbacks)
            runCatching { player?.stop() }
            runCatching { player?.release() }
        }
    }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(story.authorName) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!story.text.isNullOrBlank()) {
                val storyBackground = runCatching {
                    Color(android.graphics.Color.parseColor(story.backgroundColor ?: "#E4F4F1"))
                }.getOrDefault(Color(0xFFE4F4F1))
                Text(story.text, color = Ink, modifier = Modifier.fillMaxWidth().background(storyBackground).padding(18.dp))
            }
            story.media?.url?.let { StoryMediaWithOverlays(story) }
            story.music?.let { music ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(Color(0xFFF4F7FB)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (music.coverUrl.isNullOrBlank()) {
                            Box(Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)).background(SoftBrand), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.MusicNote, "Nhac", tint = Brand)
                            }
                        } else {
                            RemoteImage(music.coverUrl, Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(music.title, color = Ink, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(music.artist ?: "Khong ro nghe si", color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(enabled = musicToggle != null, onClick = { musicToggle?.invoke() }) {
                            Icon(if (musicPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (musicPlaying) "Tam dung nhac" else "Phat nhac", tint = Brand)
                        }
                        IconButton(onClick = { musicMuted = !musicMuted; musicSetMuted?.invoke(musicMuted) }) {
                            Icon(if (musicMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp, if (musicMuted) "Bat am thanh" else "Tat am thanh", tint = Brand)
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(enabled = hasPrevious, onClick = onPrevious) { Text("Story truoc", color = if (hasPrevious) Brand else Muted) }
                TextButton(enabled = hasNext, onClick = onNext) { Text("Story sau", color = if (hasNext) Brand else Muted) }
            }
            Text("$reactionCount lượt cảm xúc", color = Muted, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { story.id?.let { id -> scope.launch { val previous = reaction; runCatching { if (previous == "LIKE") api.removeStoryReaction(id) else api.reactStory(id, "LIKE") }.onSuccess { reaction = if (previous == "LIKE") null else "LIKE"; if (previous == null) reactionCount += 1 else if (previous == "LIKE") reactionCount = (reactionCount - 1).coerceAtLeast(0) }.onFailure { error = it.message } } } }) { Text(if (reaction == "LIKE") "Đã thích" else "Thích", color = Brand) }
                TextButton(onClick = { story.id?.let { id -> scope.launch { runCatching { api.storyViewers(id) }.onSuccess { viewers = it }.onFailure { error = it.message } } } }) { Text("Người xem", color = Brand) }
            }
            TextButton(onClick = { showReactionPicker = !showReactionPicker }) { Text("Cảm xúc", color = Brand) }
            if (showReactionPicker) {
                ReactionPicker(selected = reaction, onSelect = { selected ->
                    val id = story.id ?: return@ReactionPicker
                    scope.launch {
                        val previous = reaction
                        runCatching {
                            if (previous == selected) api.removeStoryReaction(id) else api.reactStory(id, selected)
                        }.onSuccess {
                            reaction = if (previous == selected) null else selected
                            if (previous == null && reaction != null) reactionCount += 1
                            if (previous != null && reaction == null) reactionCount = (reactionCount - 1).coerceAtLeast(0)
                            showReactionPicker = false
                        }.onFailure { error = it.message ?: "Không thể cập nhật cảm xúc" }
                    }
                }, onDismiss = { showReactionPicker = false })
            }
            viewers?.let { list -> Text(if (list.isEmpty()) "Chưa có người xem" else list.joinToString("\n") { "${it.userName}${if (it.reactionType != null) " · ${it.reactionType}" else ""}" }, color = Muted, fontSize = 12.sp) }
            OutlinedTextField(reply, { reply = it.take(5000) }, placeholder = { Text("Trả lời story...") }, singleLine = true)
            TextButton(enabled = reply.isNotBlank(), onClick = { story.id?.let { id -> scope.launch { runCatching { api.replyStory(id, reply.trim()) }.onSuccess { reply = "" }.onFailure { error = it.message } } } }) { Text("Gửi trả lời", color = Brand) }
            error?.let { Text(it, color = Color(0xFFB3261E), fontSize = 12.sp) }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Đóng", color = Brand) } })
}

@Composable
private fun MyStoriesDialog(api: ApiClient, onDismiss: () -> Unit) {
    var stories by remember { mutableStateOf<List<ApiStory>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    LaunchedEffect(Unit) { runCatching { api.myStories() }.onSuccess { stories = it }.onFailure { error = it.message } }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Story của tôi") }, text = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (stories.isEmpty() && error == null) item { Text("Chưa có story", color = Muted) }
            error?.let { item { Text(it, color = Color(0xFFB3261E)) } }
            items(stories, key = { it.id ?: it.hashCode().toLong() }) { story ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(story.text ?: story.type, color = Ink, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    TextButton(onClick = { story.id?.let { id -> scope.launch { runCatching { api.deleteStory(id) }.onSuccess { stories = stories.filterNot { it.id == id } }.onFailure { error = it.message } } } }) { Text("Xóa", color = Color(0xFFB3261E)) }
                }
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Đóng", color = Brand) } })
}

@Composable
private fun CreateStoryDialog(api: ApiClient, onDismiss: () -> Unit, onCreated: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var privacy by remember { mutableStateOf("PUBLIC") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedType by remember { mutableStateOf("TEXT") }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val picker = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        selectedType = if (uri != null && context.contentResolver.getType(uri)?.startsWith("video/") == true) "VIDEO" else "IMAGE"
    }
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text("Tạo story") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(text, { text = it.take(1000) }, label = { Text("Bạn đang nghĩ gì?") }, minLines = 3, enabled = !busy); OutlinedButton(onClick = { picker.launch("image/*") }, enabled = !busy) { Text(if (selectedUri == null) "Chọn ảnh" else "Đã chọn ảnh", color = Brand) }; OutlinedButton(onClick = { picker.launch("video/*") }, enabled = !busy) { Text("Chọn video", color = Brand) }; Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TextButton(onClick = { privacy = "PUBLIC" }) { Text("Công khai", color = if (privacy == "PUBLIC") Brand else Muted) }; TextButton(onClick = { privacy = "FRIENDS" }) { Text("Bạn bè", color = if (privacy == "FRIENDS") Brand else Muted) } }; error?.let { Text(it, color = Color(0xFFB3261E), fontSize = 12.sp) } } }, confirmButton = { TextButton(enabled = (text.isNotBlank() || selectedUri != null) && !busy, onClick = { scope.launch { busy = true; runCatching {
        if (selectedUri == null) api.createStory("TEXT", text.trim(), privacy) else {
            val resolver = context.contentResolver
            val mime = resolver.getType(selectedUri!!) ?: if (selectedType == "VIDEO") "video/*" else "image/*"
            val bytes = readValidatedUpload(context, selectedUri!!, mime)
            val upload = api.uploadMedia("story-${System.currentTimeMillis()}", mime, bytes)
            api.createStory(selectedType, text.trim().ifBlank { null }, privacy, upload.uploadId)
        }
    }.onSuccess { onCreated() }.onFailure { error = it.message ?: "Không thể đăng story" }; busy = false } }) { Text(if (busy) "Đang đăng..." else "Đăng", color = Brand) } }, dismissButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text("Hủy") } })
}

@Composable
private fun CreateStoryDialogWithMusic(api: ApiClient, onDismiss: () -> Unit, onCreated: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var overlayText by remember { mutableStateOf("") }
    var privacy by remember { mutableStateOf("PUBLIC") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedType by remember { mutableStateOf("TEXT") }
    var selectedMusic by remember { mutableStateOf<ApiStoryMusicSelection?>(null) }
    var showMusicPicker by remember { mutableStateOf(false) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val picker = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        selectedType = if (uri != null && context.contentResolver.getType(uri)?.startsWith("video/") == true) "VIDEO" else "IMAGE"
    }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Tao story") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(text, { text = it.take(1000) }, label = { Text("Ban dang nghi gi?") }, minLines = 3, enabled = !busy)
                OutlinedTextField(overlayText, { overlayText = it.take(300) }, label = { Text("Chu phu len media (tuy chon)") }, singleLine = true, enabled = !busy)
                OutlinedButton(onClick = { picker.launch("image/*") }, enabled = !busy) { Text(if (selectedUri == null) "Chon anh" else "Da chon anh", color = Brand) }
                OutlinedButton(onClick = { picker.launch("video/*") }, enabled = !busy) { Text("Chon video", color = Brand) }
                OutlinedButton(onClick = { showMusicPicker = true }, enabled = !busy) {
                    Text(selectedMusic?.let { "Nhac: ${it.track.title}" } ?: "Them nhac", color = Brand, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (selectedMusic != null) {
                    TextButton(onClick = { selectedMusic = null }, enabled = !busy) { Text("Bo nhac", color = Color(0xFFB3261E)) }
                }
                selectedMusic?.let { music ->
                    Text("${music.track.artist ?: "Khong ro nghe si"} · ${music.durationMs / 1000}s · Am luong ${(music.volume * 100).toInt()}%", color = Muted, fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { privacy = "PUBLIC" }) { Text("Cong khai", color = if (privacy == "PUBLIC") Brand else Muted) }
                    TextButton(onClick = { privacy = "FRIENDS" }) { Text("Ban be", color = if (privacy == "FRIENDS") Brand else Muted) }
                }
                error?.let { Text(it, color = Color(0xFFB3261E), fontSize = 12.sp) }
            }
        },
        confirmButton = {
            TextButton(enabled = (text.isNotBlank() || selectedUri != null) && !busy, onClick = {
                scope.launch {
                    busy = true
                    runCatching {
                        val music = selectedMusic
                        val overlays = overlayText.trim().takeIf { it.isNotBlank() }?.let { listOf(ApiStoryTextOverlay(it, x = 0.5, y = 0.5, fontSize = 28.0, color = "#FFFFFF")) } ?: emptyList()
                        if (selectedUri == null) {
                            api.createStory("TEXT", text.trim(), privacy, textOverlays = overlays, musicTrackId = music?.track?.id, musicStartMs = music?.startMs, musicDurationMs = music?.durationMs, musicVolume = music?.volume)
                        } else {
                            val resolver = context.contentResolver
                            val mime = resolver.getType(selectedUri!!) ?: if (selectedType == "VIDEO") "video/*" else "image/*"
                            val bytes = readValidatedUpload(context, selectedUri!!, mime)
                            val upload = api.uploadMedia("story-${System.currentTimeMillis()}", mime, bytes)
                            api.createStory(selectedType, text.trim().ifBlank { null }, privacy, upload.uploadId, textOverlays = overlays, musicTrackId = music?.track?.id, musicStartMs = music?.startMs, musicDurationMs = music?.durationMs, musicVolume = music?.volume)
                        }
                    }.onSuccess { onCreated() }.onFailure { error = it.message ?: "Khong the dang story" }
                    busy = false
                }
            }) { Text(if (busy) "Dang dang..." else "Dang", color = Brand) }
        },
        dismissButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text("Huy") } }
    )
    if (showMusicPicker) {
        MusicPickerDialog(
            api = api,
            initialSelection = selectedMusic,
            onSelect = { selectedMusic = it; showMusicPicker = false },
            onDismiss = { showMusicPicker = false }
        )
    }
}

@Composable
private fun RemoteStoryMedia(url: String, type: String?, modifier: Modifier = Modifier.fillMaxWidth().height(220.dp)) {
    if (type?.contains("VIDEO", true) == true) RemotePostVideo(url, modifier, videoId = "story:$url", active = true)
    else RemoteImage(url, modifier)
}

@Composable
private fun StoryMediaWithOverlays(story: ApiStory) {
    BoxWithConstraints(Modifier.fillMaxWidth().height(220.dp)) {
        story.media?.url?.let { RemoteStoryMedia(it, story.media.type, Modifier.fillMaxSize()) }
        story.textOverlays.forEach { overlay ->
            Text(
                overlay.text,
                color = runCatching { Color(android.graphics.Color.parseColor(overlay.color ?: "#FFFFFF")) }.getOrDefault(Color.White),
                fontSize = (overlay.fontSize ?: 22.0).coerceIn(10.0, 64.0).sp,
                fontWeight = if (overlay.fontStyle.equals("BOLD", true)) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .offset(x = maxWidth * overlay.x.coerceIn(0.0, 1.0).toFloat(), y = maxHeight * overlay.y.coerceIn(0.0, 1.0).toFloat())
                    .graphicsLayer { rotationZ = overlay.rotation?.toFloat() ?: 0f }
                    .background(Color.Black.copy(alpha = .35f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

internal fun loadSampledBitmapFromUrl(url: String, maxDim: Int = 1080): android.graphics.Bitmap? {
    return runCatching {
        URL(url).openStream().use { input ->
            val bytes = input.readBytes()
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            val width = bounds.outWidth
            val height = bounds.outHeight
            if (width <= 0 || height <= 0) return null

            var inSampleSize = 1
            val largest = maxOf(width, height)
            while (largest / (inSampleSize * 2) >= maxDim) {
                inSampleSize *= 2
            }
            while (largest / inSampleSize > maxDim * 1.5 && inSampleSize < 64) {
                inSampleSize *= 2
            }

            val opts = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        }
    }.getOrNull()
}

@Composable
internal fun RemoteImage(url: String, modifier: Modifier, contentScale: ContentScale = ContentScale.Crop) {
    var bitmap by remember(url) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(url) { bitmap = withContext(Dispatchers.IO) { loadSampledBitmapFromUrl(url, 1080) } }
    if (bitmap != null) Image(bitmap!!.asImageBitmap(), contentDescription = "Story media", contentScale = contentScale, modifier = modifier.clip(RoundedCornerShape(12.dp)))
    else Box(modifier.background(Color(0xFFEAF0F2)), contentAlignment = Alignment.Center) { Text("Đang tải ảnh...", color = Muted, fontSize = 12.sp) }
}

@Composable
private fun StoryBubble(name: String, letter: String, add: Boolean, color: Color, avatarUrl: String?, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(62.dp)) {
        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(color).border(2.dp, if (add) Brand else Color.White, CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
            if (avatarUrl.isNullOrBlank()) Text(letter, color = Ink, fontWeight = FontWeight.Bold, fontSize = 20.sp) else RemoteAvatar(name, avatarUrl, color, 56.dp)
            if (add) {
                Box(modifier = Modifier.align(Alignment.BottomEnd).size(20.dp).clip(CircleShape).background(Brand), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Add, "Thêm story", tint = Color.White, modifier = Modifier.size(15.dp))
                }
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(name, color = Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ComposerCard(onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).clickable(onClick = onClick), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar("M", SoftBrand)
            Spacer(Modifier.width(10.dp))
            Text("Bạn đang nghĩ gì?", color = Muted, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.Add, "Tạo bài viết", tint = Brand)
        }
    }
    Spacer(Modifier.height(14.dp))
}

data class Post(
    val id: Long?, val authorId: Long?, val name: String, val avatarUrl: String?, val time: String,
    val content: String, val media: List<ApiMedia>, val totalReactions: Int,
    val commentCount: Int, val shareCount: Int, val myReaction: String?, val reactionCounts: Map<String, Int> = emptyMap(),
    val originalPost: ApiOriginalPost? = null
)

private data class PendingUpload(
    val uploadId: Long,
    val fileName: String,
    val type: String,
    val url: String?,
    val contentType: String?,
    val fileSize: Long?
)

@Composable
private fun PendingUploadChip(upload: PendingUpload, onRemove: () -> Unit) {
    Column(modifier = Modifier.width(112.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(92.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F2F5)),
            contentAlignment = Alignment.Center
        ) {
            if (upload.type.equals("IMAGE", true) && !upload.url.isNullOrBlank()) {
                RemoteImage(upload.url, Modifier.fillMaxSize())
            } else {
                Icon(Icons.Filled.VideoLibrary, "Tep dinh kem", tint = Brand, modifier = Modifier.size(30.dp))
            }
            IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd).size(30.dp)) {
                Icon(
                    Icons.Filled.Close,
                    "Xoa tep",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp).background(Color.Black.copy(alpha = .62f), CircleShape).padding(3.dp)
                )
            }
        }
        Text(upload.fileName, color = Ink, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun ApiPost.toPost() = Post(id, authorId, authorName, authorAvatarUrl, createdAt.orEmpty(), content, media, totalReactions, commentCount, shareCount, myReaction, reactionCounts, originalPost)

@Composable
private fun SharedPostPreview(originalPost: ApiOriginalPost, active: Boolean) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clip(shape)
            .border(1.dp, Color(0xFFDADDE1), shape)
            .background(Color(0xFFF7F8FA))
    ) {
        if (!originalPost.available) {
            Text(
                "Nội dung bài viết gốc không còn khả dụng",
                color = Muted,
                fontSize = 14.sp,
                modifier = Modifier.padding(12.dp)
            )
            return@Column
        }

        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RemoteAvatar(originalPost.authorName, originalPost.authorAvatarUrl, SoftBrand, 32.dp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(originalPost.authorName, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                originalPost.createdAt?.let { Text(formatPostTime(it), color = Muted, fontSize = 11.sp) }
            }
        }
        if (originalPost.content.isNotBlank()) {
            Text(
                originalPost.content,
                color = Ink,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )
        }
        if (originalPost.media.isNotEmpty()) PostMedia(originalPost.media, originalPost.id, active)
        Spacer(Modifier.height(10.dp))
    }
}

private fun mergeComments(current: List<ApiComment>, incoming: List<ApiComment>): List<ApiComment> {
    val byId = linkedMapOf<Long, ApiComment>()
    current.forEach { comment -> comment.id?.let { byId[it] = comment } }
    incoming.forEach { comment -> comment.id?.let { byId[it] = comment } }
    return byId.values.sortedWith(compareBy<ApiComment> { it.createdAt.orEmpty() }.thenBy { it.id ?: Long.MAX_VALUE })
}

private fun removeCommentAndReplies(items: List<ApiComment>, commentId: Long): List<ApiComment> {
    val removed = mutableSetOf(commentId)
    var changed = true
    while (changed) {
        changed = false
        items.forEach { item ->
            if (item.parentCommentId in removed && item.id != null && removed.add(item.id)) changed = true
        }
    }
    return items.filterNot { it.id in removed }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
private fun PostCard(post: Post, api: ApiClient, active: Boolean = false, saved: Boolean = false, onChanged: () -> Unit = {}) {
    var reactions by remember(post.id) { mutableStateOf(post.totalReactions) }
    var comments by remember(post.id) { mutableStateOf(post.commentCount) }
    var shares by remember(post.id) { mutableStateOf(post.shareCount) }
    var reactionCounts by remember(post.id) { mutableStateOf(post.reactionCounts) }
    var myReaction by remember(post.id) { mutableStateOf(post.myReaction) }
    var showComments by remember { mutableStateOf(false) }
    var commentDraft by remember { mutableStateOf("") }
    var commentModels by remember { mutableStateOf<List<ApiComment>>(emptyList()) }
    var commentPage by remember { mutableStateOf(0) }
    var commentTotalPages by remember { mutableStateOf(0) }
    var commentTotalItems by remember(post.id) { mutableStateOf(post.commentCount.toLong()) }
    var loadingComments by remember { mutableStateOf(false) }
    var loadingMoreComments by remember { mutableStateOf(false) }
    var showCommentManager by remember { mutableStateOf(false) }
    var submittingComment by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var showActions by remember { mutableStateOf(false) }
    var showReport by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(post.content) }
    var showShare by remember { mutableStateOf(false) }
    var showShareChooser by remember { mutableStateOf(false) }
    var sharing by remember { mutableStateOf(false) }
    var shareStatus by remember(post.id) { mutableStateOf<String?>(null) }
    var showReactions by remember { mutableStateOf(false) }
    var showReactionPicker by remember { mutableStateOf(false) }
    var reactionItems by remember { mutableStateOf<List<ApiReaction>>(emptyList()) }
    var expandedContent by remember(post.id) { mutableStateOf(false) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(0.dp)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.padding(start = 14.dp).size(44.dp).border(2.dp, Brand, CircleShape)) { RemoteAvatar(post.name, post.avatarUrl, SoftBrand, 40.dp) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f).padding(vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(post.name, color = Ink, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Text("Theo dõi", color = Brand, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(formatPostTime(post.time), color = Muted, fontSize = 12.sp)
                        Text("  ·  ", color = Muted, fontSize = 12.sp)
                        Icon(Icons.Filled.Public, "Công khai", tint = Muted, modifier = Modifier.size(13.dp))
                    }
                }
                IconButton(onClick = {}) { Icon(Icons.Filled.MoreHoriz, "Tùy chọn") }
                IconButton(onClick = { showActions = true }) { Icon(Icons.Filled.Close, "Ẩn bài viết", tint = Muted) }
            }
            Text(post.content, color = Ink, fontSize = 16.sp, lineHeight = 23.sp, maxLines = if (expandedContent) Int.MAX_VALUE else 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 14.dp).clickable { expandedContent = !expandedContent })
            if (!expandedContent && post.content.length > 120) Text("… xem thêm", color = Muted, fontSize = 14.sp, modifier = Modifier.padding(start = 14.dp, top = 2.dp).clickable { expandedContent = true })
            post.originalPost?.let { originalPost -> SharedPostPreview(originalPost, active) }
            if (post.media.isNotEmpty()) PostMedia(post.media, post.id, active)
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                ReactionCountSummary(reactionCounts, reactions, onClick = {
                    val id = post.id ?: return@ReactionCountSummary
                    scope.launch { runCatching { reactionItems = api.postReactions(id); showReactions = true }.onFailure { actionError = it.message ?: "Khong the tai luot cam xuc" } }
                })
                Text("$comments bình luận  ·  $shares chia sẻ", color = Muted, fontSize = 12.sp)
            }
            Divider(color = Color(0xFFE4E6EB))
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.Start) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                PostAction(
                    if (myReaction != null) Icons.Filled.ThumbUp else Icons.Filled.FavoriteBorder,
                    if (myReaction != null) "Đã thích" else "Thích",
                    Modifier.fillMaxWidth(),
                    onLongClick = { showReactionPicker = true }
                ) {
                    val id = post.id ?: return@PostAction
                    scope.launch {
                        actionError = null
                        runCatching {
                            val previous = myReaction
                            if (previous != null) {
                                api.removeReaction(id)
                                reactionCounts = reactionCounts.adjustReaction(previous, -1)
                                myReaction = null
                                reactions = (reactions - 1).coerceAtLeast(0)
                            } else {
                                api.react(id, "LIKE")
                                reactionCounts = reactionCounts.adjustReaction("LIKE", 1)
                                reactions += 1
                                myReaction = "LIKE"
                            }
                        }
                            .onFailure { actionError = it.message ?: "Không thể cập nhật lượt thích" }
                    }
                }
                PostReactionPicker(
                    expanded = showReactionPicker,
                    selected = myReaction,
                    onSelect = { type ->
                        showReactionPicker = false
                        val id = post.id ?: return@PostReactionPicker
                        scope.launch {
                            runCatching {
                                val previous = myReaction
                                if (previous == type) {
                                    api.removeReaction(id)
                                    reactions = (reactions - 1).coerceAtLeast(0)
                                    reactionCounts = reactionCounts.adjustReaction(type, -1)
                                    myReaction = null
                                } else {
                                    api.react(id, type)
                                    if (previous == null) reactions += 1
                                    else reactionCounts = reactionCounts.adjustReaction(previous, -1)
                                    reactionCounts = reactionCounts.adjustReaction(type, 1)
                                    myReaction = type
                                }
                            }.onFailure { actionError = it.message ?: "Khong the cap nhat cam xuc" }
                        }
                    },
                    onDismiss = { showReactionPicker = false }
                )
                }
                PostAction(Icons.Filled.ChatBubbleOutline, "Bình luận", Modifier.weight(1f)) {
                    val id = post.id ?: return@PostAction
                    VideoPlaybackManager.pauseAll()
                    showComments = true
                    scope.launch {
                        loadingComments = true
                        runCatching { api.comments(id) }
                            .onSuccess { loaded ->
                                commentModels = loaded.items
                                commentPage = loaded.page
                                commentTotalPages = loaded.totalPages
                                commentTotalItems = loaded.totalItems
                            }
                            .onFailure { actionError = it.message ?: "Khong the tai binh luan" }
                        loadingComments = false
                    }
                }
                PostAction(Icons.Filled.Share, "Chia sẻ", Modifier.weight(1f)) { showShareChooser = true }
            }
            if (sharing) Text("Đang chia sẻ...", color = Brand, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            shareStatus?.let { Text(it, color = Brand, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) }
            actionError?.let { Text(it, color = Color(0xFFB3261E), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) }
        }
    }
    if (showShareChooser) ShareChoiceSheet(
        onDismiss = { showShareChooser = false },
        onShareNow = {
            if (sharing) return@ShareChoiceSheet
            showShareChooser = false
            val id = post.id ?: return@ShareChoiceSheet
            scope.launch {
                sharing = true
                actionError = null
                shareStatus = null
                runCatching { api.sharePost(id) }
                    .onSuccess { result ->
                        shares = result.sharesCount ?: (shares + 1)
                        shareStatus = "Đã chia sẻ bài viết"
                        onChanged()
                    }
                    .onFailure { actionError = it.message ?: "Không thể chia sẻ bài viết" }
                sharing = false
            }
        },
        onShareWithContent = { showShareChooser = false; showShare = true }
    )
    if (showShare) SharePostDialog(
        api,
        post.id,
        onDismiss = { showShare = false },
        onShared = { result ->
            showShare = false
            shares = result.sharesCount ?: (shares + 1)
            shareStatus = "Đã chia sẻ bài viết"
            onChanged()
        }
    )
    if (showReactions) AlertDialog(onDismissRequest = { showReactions = false }, title = { Text("Luot thich") }, text = {
        LazyColumn(modifier = Modifier.height(260.dp)) { if (reactionItems.isEmpty()) item { Text("Chua co luot thich", color = Muted) } else items(reactionItems) { reaction -> Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) { Avatar(reaction.userName.take(1), SoftBrand, 34.dp); Spacer(Modifier.width(8.dp)); Column { Text(reaction.userName, color = Ink, fontWeight = FontWeight.Bold); Text(reaction.type, color = Muted, fontSize = 12.sp) } } } }
    }, confirmButton = { TextButton(onClick = { showReactions = false }) { Text("Dong") } })
    if (showActions) AlertDialog(onDismissRequest = { showActions = false }, title = { Text("Thao tac bai viet") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            TextButton(onClick = { showActions = false; showEdit = true }) { Text("Sua bai viet") }
            TextButton(onClick = { val id = post.id ?: return@TextButton; showActions = false; scope.launch { runCatching { if (saved) api.unsavePost(id) else api.savePost(id) }.onSuccess { onChanged() }.onFailure { actionError = it.message } } }) { Text(if (saved) "Bo luu bai viet" else "Luu bai viet") }
            TextButton(onClick = { if (post.id != null) { showActions = false; showReport = true } }) { Text("Bao cao bai viet") }
            TextButton(onClick = { val id = post.id ?: return@TextButton; showActions = false; scope.launch { runCatching { api.hidePost(id) }.onSuccess { onChanged() }.onFailure { actionError = it.message } } }) { Text("An bai viet") }
            TextButton(onClick = { val id = post.id ?: return@TextButton; showActions = false; scope.launch { runCatching { api.deletePost(id) }.onSuccess { onChanged() }.onFailure { actionError = it.message } } }) { Text("Xoa bai viet", color = Color(0xFFB3261E)) }
        }
    }, confirmButton = { TextButton(onClick = { showActions = false }) { Text("Dong") } })
    if (showEdit) AlertDialog(onDismissRequest = { showEdit = false }, title = { Text("Sua bai viet") }, text = { OutlinedTextField(value = editText, onValueChange = { editText = it.take(5000) }, minLines = 4, modifier = Modifier.fillMaxWidth()) }, confirmButton = { TextButton(enabled = editText.isNotBlank(), onClick = { val id = post.id ?: return@TextButton; scope.launch { runCatching { api.updatePost(id, editText.trim()) }.onSuccess { showEdit = false; onChanged() }.onFailure { actionError = it.message } } }) { Text("Luu", color = Brand) } }, dismissButton = { TextButton(onClick = { showEdit = false }) { Text("Huy") } })
    if (showReport) ReportDialog(
        title = "Bao cao bai viet",
        onDismiss = { showReport = false },
        onSubmit = { reason, description ->
            val id = post.id ?: return@ReportDialog
            scope.launch {
                runCatching { api.reportPost(id, reason, description) }
                    .onSuccess { showReport = false; actionError = "Da gui bao cao" }
                    .onFailure { actionError = it.message ?: "Khong the gui bao cao" }
            }
        }
    )
    if (showComments) ModalBottomSheet(onDismissRequest = { showComments = false }) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp)) {
            Text("Bình luận", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("$commentTotalItems bình luận", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))
            CommentSummary(reactions = reactions, reactionCounts = reactionCounts, shares = shares)
            if (loadingComments) {
                Text("Đang tải bình luận...", color = Muted, modifier = Modifier.padding(vertical = 20.dp))
            } else if (commentModels.isEmpty()) {
                Text("Chưa có bình luận. Hãy là người đầu tiên chia sẻ ý kiến.", color = Muted, modifier = Modifier.padding(vertical = 24.dp))
            } else {
                LazyColumn(Modifier.heightIn(max = 420.dp)) {
                    items(commentModels.filter { it.parentCommentId == null }, key = { it.id ?: it.hashCode().toLong() }) { comment ->
                        CommentTreeItem(comment, commentModels, onReply = { showCommentManager = true })
                    }
                    if (commentPage + 1 < commentTotalPages) item {
                        TextButton(enabled = !loadingMoreComments, onClick = {
                            val id = post.id ?: return@TextButton
                            scope.launch {
                                loadingMoreComments = true
                                runCatching { api.comments(id, commentPage + 1) }
                                    .onSuccess { loaded ->
                                        commentModels = mergeComments(commentModels, loaded.items)
                                        commentPage = loaded.page
                                        commentTotalPages = loaded.totalPages
                                        commentTotalItems = loaded.totalItems
                                    }
                                    .onFailure { actionError = it.message ?: "Khong the tai them binh luan" }
                                loadingMoreComments = false
                            }
                        }) { Text(if (loadingMoreComments) "Đang tải..." else "Xem thêm bình luận", color = Brand) }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Avatar("T", SoftBrand, 36.dp)
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(value = commentDraft, onValueChange = { commentDraft = it }, placeholder = { Text("Viết bình luận...") }, enabled = !submittingComment, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(22.dp))
                TextButton(enabled = commentModels.isNotEmpty(), onClick = { showCommentManager = true }) { Text("Quan ly", color = Brand, fontSize = 11.sp) }
                TextButton(enabled = commentDraft.isNotBlank() && !submittingComment, onClick = {
                    val id = post.id ?: return@TextButton
                    val text = commentDraft.trim()
                    scope.launch {
                        submittingComment = true
                        runCatching { api.comment(id, text) }
                            .onSuccess { created ->
                                commentModels = mergeComments(commentModels, listOf(created))
                                commentTotalItems += 1
                                comments = commentTotalItems.toInt()
                                commentDraft = ""
                            }
                            .onFailure { actionError = it.message ?: "Không thể gửi bình luận" }
                        submittingComment = false
                    }
                }) { Text("Gửi", color = Brand, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
    if (showCommentManager) CommentManagerDialog(
        api = api,
        initial = commentModels,
        onDismiss = { showCommentManager = false },
        onReload = { post.id?.let { api.comments(it) } ?: ApiCommentPage(emptyList(), 0, 20, 0, 0) },
        onChanged = { updated, loaded ->
            commentModels = updated
            loaded?.let {
                commentPage = it.page
                commentTotalPages = it.totalPages
                commentTotalItems = it.totalItems
            }
            comments = commentTotalItems.toInt()
        }
    )
}

@Composable
private fun ReactionPicker(selected: String?, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    val reactions = listOf("LIKE" to "👍", "LOVE" to "❤️", "HAHA" to "😂", "WOW" to "😮", "SAD" to "😢", "ANGRY" to "😡")
    Box(Modifier.fillMaxWidth().height(48.dp).clickable { onDismiss() }, contentAlignment = Alignment.TopCenter) {
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(24.dp)),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                reactions.forEach { (type, emoji) ->
                    Text(emoji, fontSize = if (selected == type) 30.sp else 25.sp, modifier = Modifier.clickable { onSelect(type) }.padding(2.dp))
                }
            }
        }
    }
}

@Composable
private fun PostReactionPicker(
    expanded: Boolean,
    selected: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = androidx.compose.ui.unit.DpOffset(0.dp, (-64).dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            POST_REACTION_TYPES.forEach { type ->
                TextButton(onClick = { onSelect(type) }) {
                    Text(
                        chatReactionEmoji(type),
                        fontSize = if (selected == type) 30.sp else 26.sp
                    )
                }
            }
        }
    }
}

private fun Map<String, Int>.adjustReaction(type: String, delta: Int): Map<String, Int> {
    val key = type.uppercase()
    val next = (this[key] ?: 0) + delta
    return toMutableMap().apply { this[key] = next.coerceAtLeast(0) }
}

@Composable
private fun ReactionCountSummary(counts: Map<String, Int>, total: Int, onClick: (() -> Unit)? = null) {
    val items = listOf(
        "LIKE" to "👍",
        "LOVE" to "❤️",
        "HAHA" to "😂",
        "WOW" to "😮"
    )
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically, modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier) {
        items.forEach { (type, emoji) ->
            Text("$emoji ${compactCount(counts[type] ?: 0)}", color = Muted, fontSize = 11.sp)
        }
        Text("· ${compactCount(total)}", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun ReportDialog(title: String, onDismiss: () -> Unit, onSubmit: (String, String?) -> Unit) {
    var reason by remember { mutableStateOf("SPAM") }
    var description by remember { mutableStateOf("") }
    val reasons = listOf(
        "SPAM" to "Spam",
        "HARASSMENT" to "Quay roi",
        "HATE_SPEECH" to "Ngon tu thu ghet",
        "VIOLENCE" to "Bao luc",
        "SEXUAL_CONTENT" to "Noi dung nhay cam",
        "SCAM" to "Lua dao",
        "FALSE_INFORMATION" to "Thong tin sai",
        "OTHER" to "Ly do khac"
    )
    val descriptionRequired = reason == "OTHER"
    val canSubmit = !descriptionRequired || description.isNotBlank()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                reasons.forEach { (value, label) ->
                    Row(Modifier.fillMaxWidth().clickable { reason = value }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = reason == value, onClick = { reason = value })
                        Text(label, color = Ink)
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it.take(1000) },
                    label = { Text("Mo ta them (tuy chon)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                if (descriptionRequired && description.isBlank()) {
                    Text("Vui long mo ta cu the cho ly do khac", color = Color(0xFFB3261E), fontSize = 12.sp)
                }
            }
        },
        confirmButton = { TextButton(enabled = canSubmit, onClick = { onSubmit(reason, description.trim().ifBlank { null }) }) { Text("Gui bao cao", color = Brand) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Huy") } }
    )
}

@Composable
private fun CommentSummary(reactions: Int, reactionCounts: Map<String, Int>, shares: Int) {
    Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        ReactionCountSummary(reactionCounts, reactions)
        Text("$shares chia se", color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun CommentItem(comment: ApiComment, onReply: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            RemoteAvatar(comment.authorName, comment.authorAvatarUrl, SoftBrand, 36.dp)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Surface(color = Color(0xFFF0F2F5), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(comment.authorName, color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(formatPostTime(comment.createdAt.orEmpty()), color = Muted, fontSize = 11.sp)
                        }
                        Text(comment.content, color = Ink, fontSize = 14.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(start = 10.dp, top = 3.dp)) {
                    Text("Thich", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Tra loi", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onReply))
                    Text("👎", color = Muted, fontSize = 11.sp)
                }
            }
        }
        if (comment.parentCommentId != null) Text("  Phan hoi", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(start = 48.dp, top = 2.dp))
    }
}

@Composable
private fun CommentTreeItem(
    comment: ApiComment,
    allComments: List<ApiComment>,
    onReply: () -> Unit,
    depth: Int = 0
) {
    CommentItem(comment, onReply = onReply, modifier = Modifier.padding(start = (depth.coerceAtMost(6) * 24).dp))
    if (depth < 6) {
        allComments.filter { it.parentCommentId == comment.id }.forEach { child ->
            CommentTreeItem(child, allComments, onReply, depth + 1)
        }
    }
}

@Composable
private fun CommentManagerDialog(
    api: ApiClient,
    initial: List<ApiComment>,
    onDismiss: () -> Unit,
    onReload: (suspend () -> ApiCommentPage)? = null,
    onChanged: (List<ApiComment>, ApiCommentPage?) -> Unit
) {
    var items by remember(initial) { mutableStateOf(initial) }
    var editTarget by remember { mutableStateOf<ApiComment?>(null) }
    var draft by remember { mutableStateOf("") }
    var replyTarget by remember { mutableStateOf<ApiComment?>(null) }
    var reportTarget by remember { mutableStateOf<ApiComment?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Quan ly binh luan") }, text = {
        Column {
            LazyColumn(modifier = Modifier.height(300.dp)) { items(items, key = { it.id ?: it.hashCode().toLong() }) { item ->
                Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Text(item.authorName, color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(item.content, color = Ink)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = { draft = item.content; editTarget = item }) { Text("Sua", fontSize = 11.sp) }
                        TextButton(onClick = { replyTarget = item; draft = "" }) { Text("Tra loi", fontSize = 11.sp) }
                        TextButton(onClick = { reportTarget = item }) { Text("Bao cao", color = Muted, fontSize = 11.sp) }
                        item.id?.let { id -> TextButton(onClick = {
                            scope.launch {
                                runCatching { api.deleteComment(id) }
                                    .onSuccess {
                                        if (onReload != null) {
                                            runCatching { onReload() }
                                                .onSuccess { loaded -> items = loaded.items; onChanged(items, loaded) }
                                                .onFailure { error = it.message ?: "Không thể tải lại bình luận" }
                                        } else {
                                            items = removeCommentAndReplies(items, id)
                                            onChanged(items, null)
                                        }
                                    }
                                    .onFailure { error = it.message ?: "Không thể xóa bình luận" }
                            }
                        }) { Text("Xóa", color = Color(0xFFB3261E), fontSize = 11.sp) } }
                    }
                }
            } }
            error?.let { Text(it, color = Color(0xFFB3261E), fontSize = 12.sp) }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Dong") } })
    if (editTarget != null || replyTarget != null) AlertDialog(
        onDismissRequest = { editTarget = null; replyTarget = null },
        title = { Text(if (editTarget != null) "Sửa bình luận" else "Trả lời bình luận") },
        text = { OutlinedTextField(value = draft, onValueChange = { draft = it.take(2000) }, minLines = 2, modifier = Modifier.fillMaxWidth()) },
        confirmButton = {
            TextButton(enabled = draft.isNotBlank(), onClick = {
                scope.launch {
                    runCatching { if (editTarget != null) api.updateComment(editTarget!!.id!!, draft.trim()) else api.replyToComment(replyTarget!!.id!!, draft.trim()) }
                        .onSuccess { response ->
                            if (onReload != null) {
                                runCatching { onReload() }
                                    .onSuccess { loaded -> items = loaded.items; onChanged(items, loaded) }
                                    .onFailure { error = it.message ?: "Không thể tải lại bình luận" }
                            } else {
                                items = if (editTarget != null) items.map { if (it.id == response.id) response else it } else mergeComments(items, listOf(response))
                                onChanged(items, null)
                            }
                            editTarget = null
                            replyTarget = null
                            draft = ""
                        }
                        .onFailure { error = it.message ?: "Không thể cập nhật bình luận" }
                }
            }) { Text("Gửi", color = Brand) }
        },
        dismissButton = { TextButton(onClick = { editTarget = null; replyTarget = null }) { Text("Hủy") } }
    )
    reportTarget?.let { target ->
        ReportDialog(
            title = "Báo cáo bình luận",
            onDismiss = { reportTarget = null },
            onSubmit = { reason, description ->
                val id = target.id
                if (id == null) {
                    reportTarget = null
                } else {
                    scope.launch {
                        runCatching { api.reportComment(id, reason, description) }
                            .onSuccess { reportTarget = null; error = "Đã gửi báo cáo" }
                            .onFailure { error = it.message ?: "Không thể gửi báo cáo" }
                    }
                }
            }
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun PostAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    if (onLongClick == null) {
        TextButton(onClick = onClick, modifier = modifier) {
            Icon(icon, label, tint = Muted, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(5.dp))
            Text(label, color = Muted, fontSize = 12.sp)
        }
    } else {
        Box(
            modifier = modifier
                .height(48.dp)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, label, tint = Muted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(5.dp))
                Text(label, color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
internal fun RemoteAvatar(name: String, url: String?, color: Color, size: androidx.compose.ui.unit.Dp = 42.dp) {
    var bitmap by remember(url) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(url) { if (!url.isNullOrBlank()) bitmap = withContext(Dispatchers.IO) { loadSampledBitmapFromUrl(url, 256) } }
    if (bitmap != null) Image(bitmap!!.asImageBitmap(), contentDescription = name, modifier = Modifier.size(size).clip(CircleShape))
    else Avatar(name.take(1).ifBlank { "U" }, color, size)
}

@Composable
private fun PostMedia(media: List<ApiMedia>, ownerId: Long?, active: Boolean) {
    val visible = media.filter { !it.url.isNullOrBlank() }.take(6)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        visible.chunked(if (visible.size == 1) 1 else 2).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                rowItems.forEach { item ->
                    MediaTile(item, ownerId, active, Modifier.weight(1f))
                }
                if (rowItems.size == 1 && visible.size > 1) Spacer(Modifier.weight(1f))
            }
        }
        media.filter { it.url.isNullOrBlank() }.forEach { item ->
            Text(item.originalFileName ?: "Tệp đính kèm", color = Brand, fontSize = 13.sp, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
private fun MediaTile(item: ApiMedia, ownerId: Long?, active: Boolean, modifier: Modifier) {
    val url = item.url ?: return
    when (item.type.uppercase()) {
        "VIDEO" -> RemotePostVideo(url, modifier, videoId = "feed:${ownerId ?: "unknown"}:$url", active = active)
        "IMAGE" -> RemotePostImage(url, item.originalFileName, modifier)
        else -> Text(item.originalFileName ?: "Tệp đính kèm", color = Brand, fontSize = 13.sp, modifier = modifier.padding(8.dp))
    }
}

@Composable
private fun RemotePostImage(url: String, name: String?, modifier: Modifier = Modifier.fillMaxWidth()) {
    var bitmap by remember(url) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(url) { bitmap = withContext(Dispatchers.IO) { loadSampledBitmapFromUrl(url, 1080) } }
    if (bitmap != null) Image(bitmap!!.asImageBitmap(), contentDescription = name ?: "Ảnh bài viết", contentScale = ContentScale.Crop, modifier = modifier.aspectRatio(4f / 5f).clip(RoundedCornerShape(4.dp)))
    else Box(modifier.aspectRatio(4f / 5f).background(Color(0xFFF0F3F5)), contentAlignment = Alignment.Center) { Text("Đang tải ảnh...", color = Muted, fontSize = 12.sp) }
}

@Composable
private fun ChatResourceTile(attachment: ApiMessageAttachment) {
    val url = attachment.url
    Box(
        Modifier.width(96.dp).aspectRatio(1f).clip(RoundedCornerShape(10.dp)).background(if (attachment.type.equals("IMAGE", true)) Color(0xFFF0F2F5) else SoftBrand),
        contentAlignment = Alignment.Center
    ) {
        if (attachment.type.equals("IMAGE", true) && !url.isNullOrBlank()) {
            RemotePostImage(url, attachment.originalFileName, Modifier.fillMaxSize())
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(if (attachment.type.equals("VIDEO", true)) Icons.Filled.VideoLibrary else Icons.Filled.Add, "Tài nguyên", tint = Brand)
                Text(attachment.originalFileName ?: "Tệp", color = Ink, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 6.dp))
            }
        }
    }
}

@Composable
private fun MessageAttachments(attachments: List<ApiMessageAttachment>, own: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = if (own) Alignment.End else Alignment.Start) {
        attachments.forEach { attachment ->
            val url = attachment.url
            when {
                attachment.type.equals("IMAGE", true) && !url.isNullOrBlank() ->
                    RemotePostImage(url, attachment.originalFileName, Modifier.width(240.dp))
                attachment.type.equals("VIDEO", true) && !url.isNullOrBlank() ->
                    Box(Modifier.width(240.dp).height(150.dp).clip(RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        RemotePostVideo(url, Modifier.fillMaxSize(), videoId = "chat:$url", active = false)
                        Box(Modifier.size(42.dp).clip(CircleShape).background(Color.Black.copy(alpha = .58f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.VideoLibrary, "Video đính kèm", tint = Color.White)
                        }
                    }
                else ->
                    Row(Modifier.fillMaxWidth().background(if (own) Color.White.copy(alpha = .14f) else SoftBrand, RoundedCornerShape(8.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Add, "Tệp đính kèm", tint = if (own) Color.White else Brand)
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text(attachment.originalFileName ?: "Tệp đính kèm", color = if (own) Color.White else Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            attachment.fileSize?.let { Text(formatFileSize(it), color = if (own) Color.White.copy(alpha = .75f) else Muted, fontSize = 11.sp) }
                        }
                    }
            }
        }
    }
}

@Composable
internal fun RemotePostVideo(url: String, modifier: Modifier, videoId: String = url, active: Boolean = false) {
    var registeredView by remember(videoId) { mutableStateOf<VideoView?>(null) }
    AndroidView(
        modifier = modifier.aspectRatio(16f / 9f).clip(RoundedCornerShape(4.dp)),
        factory = { context ->
            VideoView(context).apply {
                registeredView = this
                setMediaController(MediaController(context))
                setVideoURI(Uri.parse(url))
                setOnPreparedListener { player ->
                    player.isLooping = true
                    VideoPlaybackManager.register(videoId, this)
                    if (active) VideoPlaybackManager.play(videoId)
                }
            }
        },
        update = { view ->
            VideoPlaybackManager.register(videoId, view)
            if (active) VideoPlaybackManager.play(videoId) else VideoPlaybackManager.pause(videoId)
        }
    )
    DisposableEffect(videoId) { onDispose { registeredView?.let { VideoPlaybackManager.unregister(videoId, it); it.stopPlayback() }; registeredView = null } }
}

private fun formatPostTime(value: String): String = runCatching {
    OffsetDateTime.parse(value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
}.recoverCatching {
    LocalDateTime.parse(value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
}.getOrElse { "Vừa xong" }

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1024L * 1024L -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    bytes >= 1024L -> String.format("%.0f KB", bytes / 1024.0)
    else -> "$bytes B"
}

private fun maxUploadBytes(contentType: String): Long = when {
    contentType.startsWith("image/", true) -> 10L * 1024L * 1024L
    contentType.startsWith("video/", true) -> 50L * 1024L * 1024L
    else -> 20L * 1024L * 1024L
}

/** Matches the backend limits before loading the upload into memory. */
internal fun readValidatedUpload(context: android.content.Context, uri: Uri, contentType: String): ByteArray {
    val maxBytes = maxUploadBytes(contentType)
    val declaredSize = runCatching { context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L }.getOrDefault(-1L)
    require(declaredSize <= 0L || declaredSize <= maxBytes) { "Tệp vượt quá giới hạn ${formatFileSize(maxBytes)}" }
    val output = ByteArrayOutputStream(minOf(maxBytes, 1024L * 1024L).toInt())
    context.contentResolver.openInputStream(uri)?.use { input ->
        val buffer = ByteArray(64 * 1024)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            require(total <= maxBytes) { "Tệp vượt quá giới hạn ${formatFileSize(maxBytes)}" }
            output.write(buffer, 0, read)
        }
    } ?: error("Không đọc được tệp")
    return output.toByteArray()
}

@Composable
private fun ChatScreen(
    friends: List<ApiFriend>, conversations: List<ApiConversation>, presenceByUser: Map<Long, ApiPresence>, callHistory: List<ApiCallHistory>, api: ApiClient, realtimeMessages: List<ApiMessage>, realtimeEvents: List<ChatRealtimeEvent>,
    typingByConversation: Map<Long, Long>, realtimeConnected: Boolean, realtimeResyncVersion: Long, onSendChat: (Long, String, String, Long?, List<Long>) -> Unit,
    onChatAction: (String, Long, String?) -> Unit, onStartCall: (ApiConversation, String) -> Unit = { _, _ -> }, onOpenCallHistory: () -> Unit = {}, modifier: Modifier = Modifier
) {
    var selected by remember { mutableStateOf<ApiConversation?>(null) }
    var items by remember { mutableStateOf(conversations) }
    var nextCursor by remember { mutableStateOf<String?>(null) }
    var hasMore by remember { mutableStateOf(false) }
    var loadingMore by remember { mutableStateOf(false) }
    var showNewChat by remember { mutableStateOf(false) }
    var showNewConversation by remember { mutableStateOf(false) }
    var groupMode by remember { mutableStateOf(false) }
    var recipientId by remember { mutableStateOf("") }
    var groupName by remember { mutableStateOf("") }
    var memberIds by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var remoteFriendMatches by remember { mutableStateOf<List<ApiFriend>>(emptyList()) }
    var searchingFriends by remember { mutableStateOf(false) }
    var activeFilter by remember { mutableStateOf("ALL") }
    val scope = rememberCoroutineScope()
    LaunchedEffect(conversations) { items = conversations }
    val visibleItems = items.filter { conversation ->
        val matchesSearch = searchQuery.isBlank() || conversation.name.contains(searchQuery, true) || conversation.lastMessage.contains(searchQuery, true)
        val matchesFilter = when (activeFilter) {
            "UNREAD" -> conversation.unreadCount > 0
            "GROUPS" -> isGroupConversationType(conversation.type)
            else -> true
        }
        matchesSearch && matchesFilter
    }
    val friendSearchMatches = if (searchQuery.isBlank()) emptyList() else friends
        .filter { friend -> friend.name.contains(searchQuery.trim(), true) }
        .filterNot { friend -> items.any { conversation -> conversation.name.equals(friend.name, ignoreCase = true) && !isGroupConversationType(conversation.type) } }
        .let { localMatches ->
            (localMatches + remoteFriendMatches)
                .distinctBy { it.id }
                .filterNot { friend -> items.any { conversation -> conversation.name.equals(friend.name, ignoreCase = true) && !isGroupConversationType(conversation.type) } }
        }
    LaunchedEffect(searchQuery, friends, items) {
        val term = searchQuery.trim()
        if (term.isBlank()) {
            remoteFriendMatches = emptyList()
            searchingFriends = false
        } else {
            delay(250)
            // The acceptance event may arrive before the main friends refresh completes.
            val currentFriends = if (friends.any { it.name.contains(term, true) }) {
                friends
            } else {
                runCatching { api.friends() }.getOrDefault(friends)
            }
            if (currentFriends.any { it.name.contains(term, true) }) {
                remoteFriendMatches = emptyList()
                searchingFriends = false
            } else {
                searchingFriends = true
                remoteFriendMatches = runCatching {
                    coroutineScope {
                        api.searchUsersPage(term, limit = 20).items.map { candidate ->
                            async {
                                val status = runCatching { api.friendStatus(candidate.id) }.getOrNull()
                                if (status?.status?.equals("ACCEPTED", true) == true ||
                                    status?.status?.equals("FRIEND", true) == true
                                ) {
                                    ApiFriend(candidate.id, candidate.name, candidate.avatarUrl, candidate.bio)
                                } else null
                            }
                        }.awaitAll().filterNotNull()
                    }
                }.getOrDefault(emptyList())
                searchingFriends = false
            }
        }
    }
    // Prefer the live list entry so realtime renames and avatar changes reach the open chat.
    val openConversation = selected?.let { picked -> items.firstOrNull { it.id == picked.id } ?: picked }
    val openTypingAt = openConversation?.let { typingByConversation[it.id] }
    if (openConversation != null) {
        DisposableEffect(openConversation.id) {
            onChatAction("CHAT_OPENED", openConversation.id, null)
            onDispose { onChatAction("CHAT_CLOSED", openConversation.id, null) }
        }
        LaunchedEffect(openConversation.id, openTypingAt) {
            if (openTypingAt != null) {
                val remaining = CHAT_TYPING_TIMEOUT_MS - (System.currentTimeMillis() - openTypingAt)
                if (remaining > 0) delay(remaining)
                onChatAction("TYPING_EXPIRED", openConversation.id, openTypingAt.toString())
            }
        }
    }
    if (openConversation != null) {
        ChatDetailScreen(
            friends = friends,
            presence = presenceForConversation(openConversation, friends, presenceByUser),
            conversation = openConversation,
            callHistory = callHistory,
            api = api,
            realtimeMessages = realtimeMessages,
            realtimeEvents = realtimeEvents,
            typing = openTypingAt?.let { isTypingFresh(it, System.currentTimeMillis()) } == true,
            realtimeConnected = realtimeConnected,
            realtimeResyncVersion = realtimeResyncVersion,
            onSendChat = onSendChat,
            onChatAction = onChatAction,
            onStartCall = onStartCall,
            onBack = { selected = null },
            modifier = modifier
        )
        return
    }
    Column(modifier.fillMaxSize()) {
        AppHeader(title = "Tin nhắn", action = {
            Row {
                IconButton(onClick = onOpenCallHistory) { Icon(Icons.Filled.History, "Lịch sử cuộc gọi", tint = Brand) }
                IconButton(onClick = { showNewConversation = true; error = null }) { Icon(Icons.Filled.Edit, "Tin nhắn mới") }
            }
        })
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            singleLine = true,
            placeholder = { Text("Tìm kiếm cuộc trò chuyện", color = Muted) },
            leadingIcon = { Icon(Icons.Filled.Search, "Tìm kiếm", tint = Muted) },
            shape = RoundedCornerShape(12.dp)
        )
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL" to "Tất cả", "UNREAD" to "Chưa đọc", "GROUPS" to "Nhóm").forEach { (filter, label) ->
                Surface(
                    onClick = { activeFilter = filter },
                    shape = RoundedCornerShape(10.dp),
                    color = if (activeFilter == filter) SoftBrand else Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (activeFilter == filter) Brand else Color(0xFFE4E6EB))
                ) {
                    Text(label, color = if (activeFilter == filter) Brand else Muted, fontSize = 13.sp, fontWeight = if (activeFilter == filter) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${visibleItems.size} cuộc trò chuyện", color = Muted, fontSize = 13.sp)
            TextButton(onClick = { scope.launch { runCatching { api.conversationPage(limit = 50) }.onSuccess { page -> items = page.items; nextCursor = page.nextCursor; hasMore = page.hasMore } } }) { Text("Làm mới", color = Brand) }
        }
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 20.dp)) {
            if (visibleItems.isEmpty() && friendSearchMatches.isEmpty()) item { Text("Chưa có cuộc trò chuyện phù hợp", color = Muted, modifier = Modifier.padding(24.dp)) }
            items(visibleItems, key = { it.id }) { conversation ->
                ModernConversationRow(conversation, presenceForConversation(conversation, friends, presenceByUser)) { selected = conversation }
            }
            if (friendSearchMatches.isNotEmpty()) {
                item { Text("Ban be phu hop", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)) }
                items(friendSearchMatches, key = { "friend-${it.id}" }) { friend ->
                    FriendConversationSearchRow(friend, enabled = !busy) {
                        scope.launch {
                            busy = true
                            error = null
                            runCatching { api.createDirectConversation(friend.id) }
                                .onSuccess { response ->
                                    val conversationId = response.optLong("id", response.optLong("conversationId"))
                                    if (conversationId > 0) {
                                        val placeholder = ApiConversation(conversationId, friend.name, friend.avatarUrl, "", 0, "DIRECT")
                                        items = upsertConversation(items, placeholder)
                                        runCatching { api.conversationPage(limit = 50) }
                                            .onSuccess { page ->
                                                items = upsertConversation(page.items, placeholder)
                                                nextCursor = page.nextCursor
                                                hasMore = page.hasMore
                                            }
                                        selected = items.firstOrNull { it.id == conversationId }
                                            ?: ApiConversation(conversationId, friend.name, friend.avatarUrl, "", 0, "DIRECT")
                                        searchQuery = ""
                                    }
                                }
                                .onFailure { error = it.message ?: "Khong the mo cuoc tro chuyen" }
                            busy = false
                        }
                    }
                }
            }
            if (hasMore) item {
                TextButton(enabled = !loadingMore, onClick = {
                    scope.launch { loadingMore = true; runCatching { api.conversationPage(nextCursor) }.onSuccess { page -> items = (items + page.items).distinctBy { it.id }; nextCursor = page.nextCursor; hasMore = page.hasMore }; loadingMore = false }
                }, modifier = Modifier.fillMaxWidth()) { Text(if (loadingMore) "Đang tải..." else "Tải thêm cuộc trò chuyện", color = Brand) }
            }
        }
    }
    if (showNewChat) AlertDialog(
        onDismissRequest = { if (!busy) showNewChat = false },
        title = { Text(if (groupMode) "Tạo nhóm chat" else "Bắt đầu cuộc trò chuyện") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { groupMode = false; error = null }) { Text("Chat riêng", color = if (!groupMode) Brand else Muted) }
                    TextButton(onClick = { groupMode = true; error = null }) { Text("Tạo nhóm", color = if (groupMode) Brand else Muted) }
                }
                if (groupMode) {
                    OutlinedTextField(value = groupName, onValueChange = { groupName = it }, label = { Text("Tên nhóm") }, singleLine = true, enabled = !busy)
                    OutlinedTextField(value = memberIds, onValueChange = { memberIds = it }, label = { Text("ID thành viên, cách nhau bằng dấu phẩy") }, singleLine = true, enabled = !busy)
                } else OutlinedTextField(value = recipientId, onValueChange = { recipientId = it.filter(Char::isDigit) }, label = { Text("ID người dùng") }, singleLine = true, enabled = !busy)
                error?.let { Text(it, color = Color(0xFFB3261E), fontSize = 13.sp) }
            }
        },
        confirmButton = {
            TextButton(enabled = !busy && if (groupMode) groupName.isNotBlank() && memberIds.split(',').mapNotNull { it.trim().toLongOrNull() }.isNotEmpty() else recipientId.toLongOrNull()?.let { it > 0 } == true, onClick = {
                scope.launch {
                    busy = true; error = null
                    runCatching { if (groupMode) api.createGroupConversation(groupName.trim(), memberIds.split(',').mapNotNull { it.trim().toLongOrNull() }) else api.createDirectConversation(recipientId.toLong()) }
                        .onSuccess { response ->
                            val id = response.optLong("id", response.optLong("conversationId"))
                            if (id > 0) {
                                val placeholder = ApiConversation(id, if (groupMode) groupName else "Conversation", null, "", 0, if (groupMode) "GROUP" else "DIRECT")
                                items = upsertConversation(items, placeholder)
                                runCatching { api.conversationPage(limit = 50) }.onSuccess { page -> items = upsertConversation(page.items, placeholder); nextCursor = page.nextCursor; hasMore = page.hasMore }
                                selected = items.firstOrNull { it.id == id } ?: ApiConversation(id, if (groupMode) groupName else "Cuộc trò chuyện", null, "", 0, if (groupMode) "GROUP" else "DIRECT")
                                showNewChat = false; recipientId = ""; groupName = ""; memberIds = ""
                            }
                        }.onFailure { error = it.message ?: "Không thể tạo cuộc trò chuyện" }
                    busy = false
                }
            }) { Text(if (busy) "Đang tạo..." else if (groupMode) "Tạo nhóm" else "Mở chat", color = Brand) }
        },
        dismissButton = { TextButton(enabled = !busy, onClick = { showNewChat = false }) { Text("Hủy") } }
    )
    if (showNewConversation) NewConversationSurface(
        friends = friends,
        busy = busy,
        error = error,
        onDismiss = { if (!busy) showNewConversation = false },
        onCreateDirect = { friend ->
            scope.launch {
                busy = true
                error = null
                runCatching { api.createDirectConversation(friend.id) }
                    .onSuccess { response ->
                        val id = response.optLong("id", response.optLong("conversationId"))
                        if (id > 0) {
                            val placeholder = ApiConversation(id, friend.name, friend.avatarUrl, "", 0, "DIRECT")
                            items = upsertConversation(items, placeholder)
                            val page = runCatching { api.conversationPage(limit = 50) }.getOrNull()
                            if (page != null) {
                                items = upsertConversation(page.items, placeholder)
                                nextCursor = page.nextCursor
                                hasMore = page.hasMore
                            }
                            selected = items.firstOrNull { it.id == id } ?: ApiConversation(id, friend.name, friend.avatarUrl, "", 0, "DIRECT")
                            showNewConversation = false
                        }
                    }
                    .onFailure { error = it.message ?: "Khong the mo cuoc tro chuyen" }
                busy = false
            }
        },
        onCreateGroup = { members ->
            scope.launch {
                busy = true
                error = null
                val name = members.joinToString(", ") { it.name }.take(80)
                runCatching { api.createGroupConversation(name, members.map { it.id }) }
                    .onSuccess { response ->
                        val id = response.optLong("id", response.optLong("conversationId"))
                        if (id > 0) {
                            val page = runCatching { api.conversationPage(limit = 50) }.getOrNull()
                            if (page != null) {
                                items = page.items
                                nextCursor = page.nextCursor
                                hasMore = page.hasMore
                            }
                            selected = items.firstOrNull { it.id == id } ?: ApiConversation(id, name, null, "", 0, "GROUP")
                            showNewConversation = false
                        }
                    }
                    .onFailure { error = it.message ?: "Khong the tao nhom chat" }
                busy = false
            }
        }
    )
}

@Composable
private fun FriendConversationSearchRow(friend: ApiFriend, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            RemoteAvatar(friend.name, friend.avatarUrl, SoftBrand, 56.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(friend.name, color = Ink, fontWeight = FontWeight.Bold)
                Text("Ban be - chua co cuoc tro chuyen", color = Muted, fontSize = 13.sp)
            }
            Icon(Icons.Filled.ChatBubbleOutline, "Mo cuoc tro chuyen", tint = Brand)
        }
    }
}

@Composable
private fun ConversationRow(conversation: ApiConversation, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        RemoteAvatar(conversation.name, conversation.avatarUrl, SoftBrand, 54.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Text(conversation.name, color = Ink, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis); Spacer(Modifier.width(6.dp)); Text(if (isGroupConversationType(conversation.type)) "Nhóm" else "", color = Muted, fontSize = 11.sp) }
            Text((conversation.lastMessageSenderName?.let { "$it: " } ?: "") + conversation.lastMessage.ifBlank { "Chưa có tin nhắn" }, color = Muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (conversation.unreadCount > 0) Box(Modifier.size(24.dp).clip(CircleShape).background(Brand), contentAlignment = Alignment.Center) { Text(if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun ModernConversationRow(conversation: ApiConversation, presence: ApiPresence?, onClick: () -> Unit) {
    val unread = conversation.unreadCount > 0
    Surface(
        onClick = onClick,
        color = if (unread) SoftBrand.copy(alpha = .34f) else Color.Transparent,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp)
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            Box {
                RemoteAvatar(conversation.name, conversation.avatarUrl, SoftBrand, 56.dp)
                if (presenceIsOnline(presence)) Box(Modifier.size(12.dp).align(Alignment.BottomEnd).clip(CircleShape).background(Color(0xFF22A06B)).border(2.dp, Background, CircleShape))
                if (unread) Box(Modifier.size(12.dp).align(Alignment.BottomStart).clip(CircleShape).background(Brand).border(2.dp, Background, CircleShape))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(conversation.name, color = Ink, fontWeight = if (unread) FontWeight.Bold else FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    conversation.updatedAt?.let { Text(formatPostTime(it).substringAfterLast(' '), color = Muted, fontSize = 11.sp) }
                }
                Text(
                    (conversation.lastMessageSenderName?.let { "$it: " } ?: "") + conversation.lastMessage.ifBlank { "Chưa có tin nhắn" },
                    color = if (unread) Ink else Muted,
                    fontSize = 13.sp,
                    fontWeight = if (unread) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                presenceActivityLabel(presence)?.takeIf { !presenceIsOnline(presence) }?.let {
                    Text(it, color = Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (unread) {
                Spacer(Modifier.width(8.dp))
                Box(Modifier.size(25.dp).clip(CircleShape).background(Brand), contentAlignment = Alignment.Center) { Text(if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
private fun ChatDetailScreen(
    friends: List<ApiFriend>, presence: ApiPresence?, conversation: ApiConversation, callHistory: List<ApiCallHistory>, api: ApiClient, realtimeMessages: List<ApiMessage>, realtimeEvents: List<ChatRealtimeEvent>, typing: Boolean, realtimeConnected: Boolean, realtimeResyncVersion: Long,
    onSendChat: (Long, String, String, Long?, List<Long>) -> Unit, onChatAction: (String, Long, String?) -> Unit, onStartCall: (ApiConversation, String) -> Unit,
    onBack: () -> Unit, modifier: Modifier
) {
    var messages by remember(conversation.id) { mutableStateOf<List<ApiMessage>>(emptyList()) }
    var latestPageSize by remember(conversation.id) { mutableStateOf(0) }
    var nextBeforeSequence by remember(conversation.id) { mutableStateOf<Long?>(null) }
    var hasOlder by remember(conversation.id) { mutableStateOf(false) }
    var loading by remember(conversation.id) { mutableStateOf(true) }
    var loadingOlder by remember(conversation.id) { mutableStateOf(false) }
    var error by remember(conversation.id) { mutableStateOf<String?>(null) }
    var draft by remember(conversation.id) { mutableStateOf("") }
    val typingThrottle = remember(conversation.id) { TypingThrottle() }
    var replyTo by remember(conversation.id) { mutableStateOf<ApiMessage?>(null) }
    var editingMessageId by remember(conversation.id) { mutableStateOf<Long?>(null) }
    var uploadIds by remember(conversation.id) { mutableStateOf<List<Long>>(emptyList()) }
    var uploading by remember(conversation.id) { mutableStateOf(false) }
    var pendingUploads by remember(conversation.id) { mutableStateOf<List<PendingUpload>>(emptyList()) }
    var uploadStatus by remember(conversation.id) { mutableStateOf<String?>(null) }
    var uploadError by remember(conversation.id) { mutableStateOf<String?>(null) }
    var reactionPickerMessageId by remember(conversation.id) { mutableStateOf<Long?>(null) }
    var showChatInfo by remember(conversation.id) { mutableStateOf(false) }
    var showGroupSettings by remember(conversation.id) { mutableStateOf(false) }
    var groupNameDraft by remember(conversation.id) { mutableStateOf(conversation.name) }
    var groupMemberId by remember(conversation.id) { mutableStateOf("") }
    var groupMemberQuery by remember(conversation.id) { mutableStateOf("") }
    var groupActionError by remember(conversation.id) { mutableStateOf<String?>(null) }
    var groupDetail by remember(conversation.id) { mutableStateOf<ApiConversationDetail?>(null) }
    var groupActionLoading by remember(conversation.id) { mutableStateOf(false) }
    var roleMenuMemberId by remember(conversation.id) { mutableStateOf<Long?>(null) }
    var resolvedPresence by remember(conversation.id) { mutableStateOf(presence) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) scope.launch {
            val remaining = 10 - pendingUploads.size
            if (remaining <= 0) {
                uploadError = "Mot tin nhan toi da 10 tep"
                return@launch
            }
            val selected = uris.take(remaining)
            uploadError = if (uris.size > remaining) "Chi co the them $remaining tep nua" else null
            uploading = true
            selected.forEachIndexed { index, uri ->
                uploadStatus = "Dang tai ${index + 1}/${selected.size}"
                runCatching {
                    val type = context.contentResolver.getType(uri) ?: "application/octet-stream"
                    val bytes = readValidatedUpload(context, uri, type)
                    api.uploadMedia("attachment-${System.currentTimeMillis()}-$index", type, bytes)
                }.onSuccess { upload ->
                    val id = requireNotNull(upload.uploadId) { "Backend khong tra ve ma tep" }
                    uploadIds = uploadIds + id
                    pendingUploads = pendingUploads + PendingUpload(
                        uploadId = id,
                        fileName = upload.originalFileName ?: "Tep ${index + 1}",
                        type = upload.type ?: "FILE",
                        url = upload.url,
                        contentType = upload.contentType,
                        fileSize = upload.fileSize
                    )
                }.onFailure { cause ->
                    uploadError = cause.message ?: "Khong the tai tep len"
                }
            }
            uploading = false
            uploadStatus = null
        }
    }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    var initialScrollCompleted by remember(conversation.id) { mutableStateOf(false) }
    val myId = api.currentUserId()
    val persistedCallMessages = callHistory
        .asSequence()
        .filter { it.conversationId == conversation.id }
        .map { it.toChatMessage() }
        .toList()
    fun merge(incoming: List<ApiMessage>) {
        messages = markMessagesSeenLocally(
            mergeChatMessages(messages, incoming),
            conversation.id,
            myId,
            java.time.LocalDateTime.now().toString()
        )
    }
    fun scheduleSendTimeout(clientMessageId: String, sendAttempt: Long) {
        scope.launch {
            delay(CHAT_SEND_ACK_TIMEOUT_MS)
            messages = messages.map { message ->
                if (message.clientMessageId == clientMessageId && message.id == null &&
                    message.localStatus == "PENDING" && message.localSendAttempt == sendAttempt
                ) {
                    message.copy(localStatus = "FAILED")
                } else message
            }
        }
    }
    fun retryChatMessage(message: ApiMessage) {
        val clientMessageId = message.clientMessageId ?: return
        if (!canRetryChatMessage(message)) return
        val sendAttempt = System.nanoTime()
        messages = messages.map { current ->
            if (current.clientMessageId == clientMessageId) current.copy(localStatus = "PENDING", localSendAttempt = sendAttempt) else current
        }
        onSendChat(
            conversation.id,
            message.content,
            clientMessageId,
            message.replyTo?.messageId,
            message.localUploadIds
        )
        scheduleSendTimeout(clientMessageId, sendAttempt)
    }
    val isGroup = isGroupConversationType(conversation.type)
    val canManageGroup = canManageGroup(groupDetail?.currentUserRole)
    val existingGroupMemberIds = groupDetail?.members?.map { it.userId }?.toSet().orEmpty()
    val availableGroupFriends = friends.filter { friend ->
        friend.id !in existingGroupMemberIds &&
            (groupMemberQuery.isBlank() || friend.name.contains(groupMemberQuery, true))
    }
    suspend fun loadGroupDetail() {
        if (!isGroup) {
            groupDetail = null
            return
        }
        runCatching { api.conversationDetail(conversation.id).toConversationDetail() }
            .onSuccess { detail ->
                groupDetail = detail
                groupNameDraft = detail.name
            }
            .onFailure { groupActionError = it.message ?: "Khong the tai thong tin nhom" }
    }
    LaunchedEffect(conversation.id, presence) {
        resolvedPresence = presence
        if (presence == null && !isGroup) {
            runCatching { api.conversationDetail(conversation.id).toConversationDetail() }
                .onSuccess { detail ->
                    detail.members
                        .firstOrNull { it.userId != api.currentUserId() }
                        ?.let { member ->
                            runCatching { api.presence(member.userId) }
                                .onSuccess { resolvedPresence = it }
                        }
                }
        }
    }
    fun applyRefreshedMessagePage(page: ApiMessagePage) {
        val hadOlderHistory = latestPageSize > 0 && messages.count { it.id != null } > latestPageSize
        messages = replaceLatestMessagePage(messages, page.items, latestPageSize.takeIf { it > 0 })
        latestPageSize = maxOf(latestPageSize, page.items.count { it.id != null })
        if (hadOlderHistory) {
            hasOlder = hasOlder || page.hasMore
        } else {
            nextBeforeSequence = page.nextBeforeSequence
            hasOlder = page.hasMore
        }
    }
    LaunchedEffect(conversation.id) {
        loading = true; error = null
        runCatching { api.messagePage(conversation.id) }.onSuccess { page ->
            messages = markMessagesSeenLocally(
                mergeChatMessages(page.items + persistedCallMessages, realtimeMessages.filter { it.conversationId == conversation.id }),
                conversation.id,
                myId,
                java.time.LocalDateTime.now().toString()
            )
            latestPageSize = page.items.count { it.id != null }
            nextBeforeSequence = page.nextBeforeSequence
            hasOlder = page.hasMore
        }.onFailure { error = it.message ?: "Không thể tải tin nhắn" }
        runCatching { api.markConversationSeen(conversation.id) }
        onChatAction("SEEN_CONVERSATION", conversation.id, null)
        loading = false
    }
    LaunchedEffect(conversation.id, conversation.type) { loadGroupDetail() }
    LaunchedEffect(realtimeMessages, conversation.id) { merge(realtimeMessages.filter { it.conversationId == conversation.id }) }
    LaunchedEffect(callHistory, conversation.id) { merge(persistedCallMessages) }
    LaunchedEffect(realtimeEvents.lastOrNull()?.nonce) {
        val relevant = realtimeEvents.filter {
            val eventConversationId = it.payload.optLong("conversationId")
            if (it.destination.endsWith("/queue/errors")) {
                isChatErrorRelevant(
                    errorConversationId = eventConversationId.takeIf { id -> id != 0L },
                    clientMessageId = it.payload.optStringOrNull("clientMessageId"),
                    conversationId = conversation.id,
                    messages = messages
                )
            } else {
                eventConversationId == conversation.id
            }
        }
        relevant.forEach { event ->
            if (event.destination.endsWith("/queue/errors")) {
                val clientMessageId = event.payload.optStringOrNull("clientMessageId")
                if (clientMessageId != null) {
                    messages = messages.map { message ->
                        if (message.clientMessageId == clientMessageId) message.copy(localStatus = "FAILED") else message
                    }
                }
                error = event.payload.optStringOr("message", "Khong the gui tin nhan")
                return@forEach
            }
            // chat.seen carries one messageId; chat.seenConversation lists every message it covered.
            val messageId = event.payload.optLong("messageId").takeIf { it != 0L }
            val messageIds = event.payload.optJSONArray("messageIds")?.let { ids -> (0 until ids.length()).map { ids.optLong(it) }.toSet() }.orEmpty()
            if (messageId == null && messageIds.isEmpty()) return@forEach
            messages = messages.map { message ->
                if (message.id == null || (message.id != messageId && message.id !in messageIds)) message
                else when {
                    event.destination.endsWith("messages.delivered") -> message.copy(deliveredAt = event.payload.optStringOrNull("deliveredAt"))
                    event.destination.endsWith("messages.seen") -> message.copy(seenAt = event.payload.optStringOrNull("seenAt"), deliveredAt = event.payload.optStringOrNull("deliveredAt") ?: message.deliveredAt)
                    else -> message
                }
            }
        }
        if (relevant.any { it.destination.contains("updated|recalled|deleted|reaction".toRegex()) }) {
            runCatching { api.messagePage(conversation.id) }.onSuccess { page -> applyRefreshedMessagePage(page) }
        }
    }
    LaunchedEffect(conversation.id, realtimeResyncVersion) {
        if (realtimeResyncVersion == 0L) return@LaunchedEffect
        runCatching { api.messagePage(conversation.id) }
            .onSuccess { page -> applyRefreshedMessagePage(page) }
    }
    // Only group events that arrive while this chat is open; the shared buffer also holds old ones.
    var groupEventsAfter by remember(conversation.id) { mutableStateOf(realtimeEvents.lastOrNull()?.nonce ?: 0L) }
    LaunchedEffect(realtimeEvents.lastOrNull()?.nonce) {
        val fresh = realtimeEvents.filter { it.nonce > groupEventsAfter && it.destination.endsWith("/queue/conversations.events") }
        groupEventsAfter = realtimeEvents.lastOrNull()?.nonce ?: groupEventsAfter
        val removed = fresh.any { event ->
            event.payload.toGroupConversationEvent()?.let { it.conversationId == conversation.id && it.removesUser(myId) } == true
        }
        if (removed) onBack()
    }
    val incomingIds = incomingMessageIds(messages, myId)
    LaunchedEffect(conversation.id, incomingIds) {
        incomingIds.forEach { messageId -> onChatAction("DELIVERED", messageId, null) }
        if (incomingIds.isNotEmpty()) onChatAction("SEEN_CONVERSATION", conversation.id, null)
    }
    LaunchedEffect(conversation.id, loading, messages.size, hasOlder, error, initialScrollCompleted) {
        if (shouldScrollToLatestMessageAfterOpening(loading, messages.size, initialScrollCompleted)) {
            val expectedItemCount = messages.size +
                (if (hasOlder) 1 else 0) +
                (if (error != null) 1 else 0)
            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .first { itemCount -> itemCount >= expectedItemCount }
            listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1)
            initialScrollCompleted = true
        }
    }
    Column(modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Quay lại", tint = Ink) }
            RemoteAvatar(conversation.name, conversation.avatarUrl, SoftBrand, 42.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(conversation.name, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                presenceActivityLabel(resolvedPresence)?.let { activity ->
                    Text(activity, color = if (presenceIsOnline(resolvedPresence)) Brand else Muted, fontSize = 11.sp, maxLines = 1)
                }
                Text(if (isGroupConversationType(conversation.type)) "Nhóm chat" else if (typing) "Đang nhập..." else "Tin nhắn riêng tư", color = if (typing) Brand else Muted, fontSize = 11.sp)
            }
            if (!isGroupConversationType(conversation.type)) {
                IconButton(onClick = { onStartCall(conversation, "AUDIO") }) { Icon(Icons.Filled.Call, "Gọi audio", tint = Brand) }
                IconButton(onClick = { onStartCall(conversation, "VIDEO") }) { Icon(Icons.Filled.Videocam, "Gọi video", tint = Brand) }
            }
            if (isGroupConversationType(conversation.type)) IconButton(onClick = { showGroupSettings = true; groupActionError = null }) { Icon(Icons.Filled.MoreHoriz, "Quản lý nhóm", tint = Brand) }
            IconButton(onClick = { showChatInfo = true }) { Icon(Icons.Filled.Info, "Thông tin đoạn chat", tint = Brand) }
        }
        Row(
            Modifier.fillMaxWidth().background(if (realtimeConnected) Color(0xFFEAF8F0) else Color(0xFFFFF4E5)).padding(horizontal = 18.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(if (realtimeConnected) Color(0xFF22A06B) else Color(0xFFF59E0B)))
            Spacer(Modifier.width(7.dp))
            Text(if (realtimeConnected) "Đã kết nối realtime" else "Đang kết nối lại realtime...", color = Muted, fontSize = 11.sp, modifier = Modifier.weight(1f))
            if (!realtimeConnected) TextButton(onClick = { onChatAction("RECONNECT", 0L, null) }) { Text("Thử lại", color = Brand, fontSize = 11.sp) }
        }
        if (typing) Text("Đang nhập...", color = Brand, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp))
        LaunchedEffect(draft, conversation.id) {
            if (draft.isBlank()) {
                if (typingThrottle.onStop()) onChatAction("TYPING", conversation.id, "false")
            } else {
                if (typingThrottle.onInput(System.currentTimeMillis())) onChatAction("TYPING", conversation.id, "true")
                kotlinx.coroutines.delay(CHAT_TYPING_IDLE_MS)
                if (typingThrottle.onStop()) onChatAction("TYPING", conversation.id, "false")
            }
        }
        LazyColumn(Modifier.weight(1f), state = listState, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (hasOlder) item {
                TextButton(enabled = !loadingOlder, onClick = { scope.launch { loadingOlder = true; runCatching { api.messagePage(conversation.id, nextBeforeSequence) }.onSuccess { page -> messages = mergeChatMessages(page.items, messages); nextBeforeSequence = page.nextBeforeSequence; hasOlder = page.hasMore }; loadingOlder = false } }, modifier = Modifier.fillMaxWidth()) { Text(if (loadingOlder) "Đang tải..." else "Tải tin cũ hơn", color = Brand) }
            }
            when {
                loading -> item { Text("Đang tải tin nhắn...", color = Muted) }
                messages.isEmpty() && error != null -> item { Text(error!!, color = Color(0xFFB3261E)) }
                messages.isEmpty() -> item { Text("Chưa có tin nhắn. Hãy bắt đầu cuộc trò chuyện.", color = Muted) }
                else -> {
                    error?.let { messageError -> item { Text(messageError, color = Color(0xFFB3261E), fontSize = 12.sp) } }
                    items(messages, key = { it.id ?: it.clientMessageId ?: it.hashCode() }) { message ->
                    val callPresentation = message.callChatPresentation()
                    val own = message.senderId == myId
                    val recalled = message.recalledAt != null
                    val stableMessageId = message.id
                    if (callPresentation != null) {
                        CallHistoryMessageCard(
                            conversation = conversation,
                            presentation = callPresentation,
                            canCallBack = !isGroupConversationType(conversation.type),
                            onCallBack = { onStartCall(conversation, callPresentation.callType) }
                        )
                    } else Column(Modifier.fillMaxWidth(), horizontalAlignment = if (own) Alignment.End else Alignment.Start) {
                        if (!own) Text(message.senderName, color = Muted, fontSize = 11.sp)
                        Box {
                            Card(
                            modifier = Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = { reactionPickerMessageId = message.id }
                            ),
                            colors = CardDefaults.cardColors(if (own) Brand else Color.White),
                            shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = if (own) Alignment.End else Alignment.Start) {
                                message.replyTo?.let { Text("↪ ${it.senderName}: ${it.content}", color = if (own) Color.White.copy(alpha = .75f) else Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                if (!recalled && message.attachments.isNotEmpty()) MessageAttachments(message.attachments, own)
                                Text(if (recalled) "Tin nhắn đã được thu hồi" else message.content.ifBlank { message.attachments.joinToString { it.originalFileName ?: "Tệp đính kèm" }.ifBlank { "Tệp đính kèm" } }, color = if (recalled) (if (own) Color.White.copy(alpha = .7f) else Muted) else if (own) Color.White else Ink, fontSize = 15.sp)
                                if (message.editedAt != null && !recalled) Text("Đã chỉnh sửa", color = if (own) Color.White.copy(alpha = .7f) else Muted, fontSize = 10.sp)
                                message.reactions.takeIf { it.isNotEmpty() }?.let { reactions ->
                                    Text(reactions.joinToString(" ") { reaction -> chatReactionEmoji(reaction.type) }, fontSize = 11.sp)
                                }
                                if (own) Text(
                                    when {
                                        message.localStatus == "FAILED" -> "Gửi thất bại"
                                        message.id == null -> "Đang gửi"
                                        message.seenAt != null -> "Đã xem"
                                        message.deliveredAt != null -> "Đã nhận"
                                        else -> "Đã gửi"
                                    },
                                    color = if (message.localStatus == "FAILED") Color(0xFFFFD6D6) else Color.White.copy(alpha = .7f),
                                    fontSize = 10.sp
                                )
                                if (own && canRetryChatMessage(message)) {
                                    TextButton(onClick = { retryChatMessage(message) }) {
                                        Text("Gửi lại", color = Color.White, fontSize = 11.sp)
                                    }
                                }
                                }
                            }
                            if (stableMessageId != null) {
                                DropdownMenu(
                                    expanded = reactionPickerMessageId == stableMessageId,
                                    onDismissRequest = { reactionPickerMessageId = null }
                                ) {
                                    Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        listOf("LIKE", "LOVE", "HAHA", "WOW", "SAD", "ANGRY").forEach { type ->
                                            TextButton(onClick = { onChatAction("REACTION", stableMessageId, type); reactionPickerMessageId = null }) {
                                                Text(chatReactionEmoji(type), fontSize = 17.sp)
                                            }
                                        }
                                    }
                                    DropdownMenuItem(text = { Text("Trả lời") }, onClick = { replyTo = message; reactionPickerMessageId = null })
                                    if (own && !recalled) {
                                        DropdownMenuItem(text = { Text("Sửa") }, onClick = { editingMessageId = stableMessageId; draft = message.content; reactionPickerMessageId = null })
                                        DropdownMenuItem(text = { Text("Thu hồi") }, onClick = { onChatAction("RECALL", stableMessageId, null); reactionPickerMessageId = null })
                                    }
                                    if (!recalled) DropdownMenuItem(text = { Text("Xóa phía tôi") }, onClick = { onChatAction("DELETE", stableMessageId, null); reactionPickerMessageId = null })
                                }
                            }
                        }
                    }
                }
            }
        }
        }
        editingMessageId?.let { id -> Row(Modifier.fillMaxWidth().background(SoftBrand).padding(horizontal = 14.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) { Text("Đang sửa tin nhắn #$id", Modifier.weight(1f), color = Muted); IconButton(onClick = { editingMessageId = null; draft = "" }) { Icon(Icons.Filled.Close, "Hủy sửa") } } }
        replyTo?.let { target -> Row(Modifier.fillMaxWidth().background(SoftBrand).padding(horizontal = 14.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) { Text("Trả lời ${target.senderName}: ${target.content}", Modifier.weight(1f), color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis); IconButton(onClick = { replyTo = null }) { Icon(Icons.Filled.Close, "Hủy trả lời") } } }
        if (pendingUploads.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pendingUploads.forEach { upload ->
                    PendingUploadChip(upload) {
                        pendingUploads = pendingUploads.filterNot { it.uploadId == upload.uploadId }
                        uploadIds = uploadIds.filterNot { it == upload.uploadId }
                    }
                }
            }
        }
        uploadStatus?.let { Text(it, color = Brand, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) }
        uploadError?.let { Text(it, color = Color(0xFFB3261E), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) }
        if (uploadIds.isNotEmpty()) Text("Da chon ${uploadIds.size} tep dinh kem", color = Brand, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.Bottom) {
            IconButton(enabled = !uploading && pendingUploads.size < 10, onClick = { filePicker.launch("*/*") }) { Icon(Icons.Filled.Add, "Dinh kem", tint = if (uploading) Muted else Brand) }
            OutlinedTextField(value = draft, onValueChange = { draft = it }, modifier = Modifier.weight(1f), placeholder = { Text("Viết tin nhắn...") }, maxLines = 4, shape = RoundedCornerShape(20.dp))
            IconButton(enabled = (draft.isNotBlank() || uploadIds.isNotEmpty()) && !uploading, onClick = {
                val content = draft.trim()
                val editingId = editingMessageId
                if (editingId != null) {
                    onChatAction("EDIT", editingId, content)
                    messages = messages.map { if (it.id == editingId) it.copy(content = content, editedAt = LocalDateTime.now().toString()) else it }
                    editingMessageId = null
                } else {
                    val clientId = UUID.randomUUID().toString()
                    val optimisticAttachments = pendingUploads.map { upload ->
                        ApiMessageAttachment(upload.uploadId, upload.type, upload.url, upload.fileName, upload.contentType, upload.fileSize)
                    }
                    val optimistic = ApiMessage(
                        id = null,
                        conversationId = conversation.id,
                        senderId = myId,
                        senderName = "Ban",
                        content = content,
                        messageType = if (optimisticAttachments.isEmpty()) "TEXT" else "MEDIA",
                        sentAt = LocalDateTime.now().toString(),
                        editedAt = null,
                        recalledAt = null,
                        seenAt = null,
                        clientMessageId = clientId,
                        attachments = optimisticAttachments,
                        localStatus = "PENDING",
                        localUploadIds = uploadIds,
                        localSendAttempt = System.nanoTime()
                    )
                    merge(listOf(optimistic)); onSendChat(conversation.id, content, clientId, replyTo?.id, uploadIds); scheduleSendTimeout(clientId, optimistic.localSendAttempt)
                    uploadIds = emptyList(); pendingUploads = emptyList()
                }
                if (typingThrottle.onStop()) onChatAction("TYPING", conversation.id, "false")
                draft = ""; replyTo = null
            }) { Icon(Icons.Filled.Send, "Gui", tint = if (draft.isNotBlank() || uploadIds.isNotEmpty()) Brand else Muted) }
        }
    }
    DisposableEffect(conversation.id) {
        onDispose { if (typingThrottle.onStop()) onChatAction("TYPING", conversation.id, "false") }
    }
    if (showChatInfo) {
        val resources = messages.flatMap { it.attachments }.distinctBy { it.attachmentId ?: it.url ?: it.originalFileName }
        ModalBottomSheet(onDismissRequest = { showChatInfo = false }) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    RemoteAvatar(conversation.name, conversation.avatarUrl, SoftBrand, 64.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(conversation.name, color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(if (isGroupConversationType(conversation.type)) "Nhóm chat" else "Cuộc trò chuyện riêng tư", color = Muted, fontSize = 13.sp)
                    }
                }
                Divider(color = Color(0xFFE4E6EB))
                Text("Tệp và liên kết", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (resources.isEmpty()) {
                    Text("Chưa có tài nguyên được chia sẻ", color = Muted, fontSize = 13.sp)
                } else {
                    resources.chunked(3).forEach { rowItems ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowItems.forEach { resource -> ChatResourceTile(resource) }
                            repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
                if (isGroupConversationType(conversation.type)) {
                    OutlinedButton(onClick = { showChatInfo = false; showGroupSettings = true; groupActionError = null }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.People, "Quản lý thành viên")
                        Spacer(Modifier.width(8.dp))
                        Text("Quản lý nhóm")
                    }
                }
                TextButton(onClick = { showChatInfo = false }, modifier = Modifier.fillMaxWidth()) { Text("Đóng", color = Brand) }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
    if (showGroupSettings) AlertDialog(
        onDismissRequest = { if (!groupActionLoading) showGroupSettings = false },
        title = { Text("Quản lý nhóm") },
        text = {
            Column(
                Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val detail = groupDetail
                Text(
                    when {
                        detail == null -> "Đang tải thông tin nhóm..."
                        canManageGroup -> "Vai trò của bạn: Quản trị viên"
                        else -> "Vai trò của bạn: Thành viên"
                    },
                    color = if (canManageGroup) Brand else Muted,
                    fontWeight = FontWeight.SemiBold
                )
                if (detail != null) {
                    Text("Thành viên (${detail.members.size})", color = Ink, fontWeight = FontWeight.Bold)
                    detail.members.forEach { member ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(36.dp).clip(CircleShape).background(SoftBrand),
                                contentAlignment = Alignment.Center
                            ) { Text(member.userName.take(1).uppercase(), color = Brand, fontWeight = FontWeight.Bold) }
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(member.userName, color = Ink, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(member.email ?: if (member.role.equals("ADMIN", true)) "Quản trị viên" else "Thành viên", color = Muted, fontSize = 11.sp)
                            }
                            if (canManageGroup && member.userId != myId) {
                                Box {
                                    TextButton(enabled = !groupActionLoading, onClick = { roleMenuMemberId = member.userId }) {
                                        Text(if (member.role.equals("ADMIN", true)) "Admin" else "Member", color = Brand, fontSize = 12.sp)
                                    }
                                    DropdownMenu(
                                        expanded = roleMenuMemberId == member.userId,
                                        onDismissRequest = { roleMenuMemberId = null }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Quản trị viên") },
                                            onClick = {
                                                roleMenuMemberId = null
                                                scope.launch {
                                                    groupActionLoading = true
                                                    groupActionError = null
                                                    runCatching { api.updateGroupMemberRole(conversation.id, member.userId, "ADMIN") }
                                                        .onSuccess { loadGroupDetail() }
                                                        .onFailure { groupActionError = it.message ?: "Không thể đổi quyền" }
                                                    groupActionLoading = false
                                                }
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Thành viên") },
                                            onClick = {
                                                roleMenuMemberId = null
                                                scope.launch {
                                                    groupActionLoading = true
                                                    groupActionError = null
                                                    runCatching { api.updateGroupMemberRole(conversation.id, member.userId, "MEMBER") }
                                                        .onSuccess { loadGroupDetail() }
                                                        .onFailure { groupActionError = it.message ?: "Không thể đổi quyền" }
                                                    groupActionLoading = false
                                                }
                                            }
                                        )
                                    }
                                }
                            } else {
                                Text(if (member.role.equals("ADMIN", true)) "Admin" else "Member", color = Muted, fontSize = 12.sp)
                            }
                        }
                    }
                }
                if (canManageGroup) {
                    Divider(color = Color(0xFFE4E6EB))
                    Text("Thao tác quản trị", color = Ink, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = groupNameDraft,
                        onValueChange = { groupNameDraft = it },
                        label = { Text("Tên nhóm") },
                        singleLine = true,
                        enabled = !groupActionLoading
                    )
                    OutlinedTextField(
                        value = groupMemberQuery,
                        onValueChange = {
                            groupMemberQuery = it
                            groupMemberId = ""
                        },
                        label = { Text("Tìm thành viên") },
                        placeholder = { Text("Nhập tên bạn bè") },
                        leadingIcon = { Icon(Icons.Filled.Search, "Tìm thành viên", tint = Muted) },
                        singleLine = true,
                        enabled = !groupActionLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                    availableGroupFriends.take(6).forEach { friend ->
                        val selected = groupMemberId == friend.id.toString()
                        Row(
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) SoftBrand else Color.Transparent)
                                .clickable(enabled = !groupActionLoading) {
                                    groupMemberId = friend.id.toString()
                                    groupMemberQuery = friend.name
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RemoteAvatar(friend.name, friend.avatarUrl, SoftBrand, 38.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(friend.name, color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            if (selected) Icon(Icons.Filled.Check, "Đã chọn", tint = Brand)
                        }
                    }
                    TextButton(
                        enabled = !groupActionLoading && groupMemberId.toLongOrNull()?.let { it > 0 } == true,
                        onClick = {
                            val memberId = groupMemberId.toLong()
                            scope.launch {
                                groupActionLoading = true
                                groupActionError = null
                                runCatching { api.addGroupMembers(conversation.id, listOf(memberId)) }
                                    .onSuccess { groupMemberId = ""; groupMemberQuery = ""; loadGroupDetail() }
                                    .onFailure { groupActionError = it.message ?: "Không thể thêm thành viên" }
                                groupActionLoading = false
                            }
                        }
                    ) { Text("Thêm", color = Brand) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            enabled = !groupActionLoading && groupMemberId.toLongOrNull()?.let { it > 0 && it != myId } == true,
                            onClick = {
                                val memberId = groupMemberId.toLong()
                                scope.launch {
                                    groupActionLoading = true
                                    groupActionError = null
                                    runCatching { api.removeGroupMember(conversation.id, memberId) }
                                        .onSuccess { groupMemberId = ""; loadGroupDetail() }
                                        .onFailure { groupActionError = it.message ?: "Không thể xóa thành viên" }
                                    groupActionLoading = false
                                }
                            }
                        ) { Text("Xóa ID", color = Color(0xFFB3261E)) }
                        TextButton(
                            enabled = !groupActionLoading && groupNameDraft.isNotBlank(),
                            onClick = {
                                scope.launch {
                                    groupActionLoading = true
                                    groupActionError = null
                                    runCatching { api.renameGroup(conversation.id, groupNameDraft.trim()) }
                                        .onSuccess { loadGroupDetail() }
                                        .onFailure { groupActionError = it.message ?: "Không thể đổi tên nhóm" }
                                    groupActionLoading = false
                                }
                            }
                        ) { Text("Lưu tên", color = Brand) }
                    }
                }
                TextButton(
                    enabled = !groupActionLoading,
                    onClick = {
                        scope.launch {
                            groupActionLoading = true
                            groupActionError = null
                            runCatching { api.leaveGroup(conversation.id) }
                                .onFailure { groupActionError = it.message ?: "Không thể rời nhóm" }
                                .onSuccess { showGroupSettings = false; onBack() }
                            groupActionLoading = false
                        }
                    }
                ) { Text("Rời nhóm", color = Color(0xFFB3261E)) }
                groupActionError?.let { Text(it, color = Color(0xFFB3261E), fontSize = 12.sp) }
            }
        },
        confirmButton = { TextButton(enabled = !groupActionLoading, onClick = { showGroupSettings = false }) { Text("Đóng", color = Brand) } }
    )
}

@Composable
private fun CallHistoryMessageCard(
    conversation: ApiConversation,
    presentation: CallChatPresentation,
    canCallBack: Boolean,
    onCallBack: () -> Unit
) {
    val accent = if (presentation.isProblem) Color(0xFFD93025) else Brand
    val icon = when {
        presentation.isProblem -> Icons.Filled.CallEnd
        presentation.callType == "VIDEO" -> Icons.Filled.Videocam
        else -> Icons.Filled.Call
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        RemoteAvatar(conversation.name, conversation.avatarUrl, SoftBrand, 34.dp)
        Spacer(Modifier.width(8.dp))
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE4E6EB))
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(42.dp).clip(CircleShape).background(accent.copy(alpha = .12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(21.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(presentation.title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(presentation.detail, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                }
                if (canCallBack) {
                    Button(
                        onClick = onCallBack,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Brand)
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Gọi lại")
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendsScreen(friends: List<ApiFriend>, presenceByUser: Map<Long, ApiPresence>, api: ApiClient, onOpenUserProfile: (Long) -> Unit, onFriendsChanged: (ApiFriend?) -> Unit = {}, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<ApiSearchUser>>(emptyList()) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var searching by remember { mutableStateOf(false) }
    var searchingMore by remember { mutableStateOf(false) }
    var searchPage by remember { mutableStateOf(0) }
    var searchTotalPages by remember { mutableStateOf(0) }
    var activeSection by remember { mutableStateOf("FRIENDS") }
    var received by remember { mutableStateOf<List<ApiFriendRequest>>(emptyList()) }
    var sent by remember { mutableStateOf<List<ApiFriendRequest>>(emptyList()) }
    var loadingRequests by remember { mutableStateOf(false) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    fun search() {
        val term = query.trim()
        if (term.length < 2) {
            searchError = "Nhập ít nhất 2 ký tự để tìm kiếm"
            results = emptyList()
            return
        }
        scope.launch {
            searching = true
            searchError = null
            runCatching { api.searchUsersPage(term) }
                .onSuccess { page -> results = page.items; searchPage = page.page; searchTotalPages = page.totalPages }
                .onFailure { searchError = it.message ?: "Không thể tìm kiếm" }
            searching = false
        }
    }
    fun loadMoreSearchResults() {
        val term = query.trim()
        if (term.length < 2 || searching || searchingMore || searchPage + 1 >= searchTotalPages) return
        scope.launch {
            searchingMore = true
            runCatching { api.searchUsersPage(term, searchPage + 1) }
                .onSuccess { page -> results = (results + page.items).distinctBy { it.id }; searchPage = page.page; searchTotalPages = page.totalPages }
                .onFailure { searchError = it.message ?: "Khong the tai them ket qua" }
            searchingMore = false
        }
    }
    fun loadRequests() { scope.launch { loadingRequests = true; runCatching { api.friendRequests() to api.sentFriendRequests() }.onSuccess { (incoming, outgoing) -> received = incoming; sent = outgoing }; loadingRequests = false } }
    Column(modifier.fillMaxSize()) {
        AppHeader("Bạn bè") { Icon(Icons.Filled.Search, "Tìm kiếm", tint = Brand) }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it; searchError = null; results = emptyList(); searchPage = 0; searchTotalPages = 0 },
            placeholder = { Text("Tìm người dùng") },
            singleLine = true,
            trailingIcon = { IconButton(onClick = { search() }, enabled = !searching) { Icon(Icons.Filled.Search, "Thực hiện tìm kiếm", tint = Brand) } },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            shape = RoundedCornerShape(16.dp)
        )
        searchError?.let { Text(it, color = Color(0xFFB3261E), fontSize = 13.sp, modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) }
        Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {}, colors = ButtonDefaults.buttonColors(Brand), shape = RoundedCornerShape(14.dp)) { Text("Gợi ý") }
            OutlinedButton(onClick = {}, shape = RoundedCornerShape(14.dp)) { Text("Lời mời", color = Brand) }
        }
        Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { activeSection = "FRIENDS" }) { Text("Ban be", color = Brand) }
            TextButton(onClick = { activeSection = "RECEIVED"; loadRequests() }) { Text("Loi moi", color = Brand) }
            TextButton(onClick = { activeSection = "SENT"; loadRequests() }) { Text("Da gui", color = Brand) }
        }
        Spacer(Modifier.height(14.dp))
        Text(if (query.trim().length >= 2) "Kết quả tìm kiếm" else "Bạn bè", modifier = Modifier.padding(horizontal = 18.dp), color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        LazyColumn {
            if (searching) item { Text("Đang tìm kiếm...", color = Muted, modifier = Modifier.padding(24.dp)) }
            else if (query.trim().length >= 2 && results.isEmpty() && searchError == null) item { Text("Không tìm thấy người dùng", color = Muted, modifier = Modifier.padding(24.dp)) }
            else if (query.trim().length >= 2) {
                items(results, key = { it.id }) { person -> SearchUserRow(person, api, onOpenUserProfile) }
                if (searchPage + 1 < searchTotalPages) item {
                    TextButton(enabled = !searchingMore, onClick = ::loadMoreSearchResults, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        if (searchingMore) CircularProgressIndicator(Modifier.size(16.dp), color = Brand, strokeWidth = 2.dp)
                        else Text("Tai them ket qua", color = Brand)
                    }
                }
            }
            else if (activeSection == "FRIENDS" && friends.isEmpty()) item { Text("Chưa có dữ liệu bạn bè", color = Muted, modifier = Modifier.padding(24.dp)) }
            else if (activeSection == "RECEIVED") { if (loadingRequests) item { Text("Dang tai...", color = Muted, modifier = Modifier.padding(24.dp)) }; items(received, key = { it.requestId }) { request -> ReceivedRequestRow(request, api, onChanged = { accepted -> loadRequests(); onFriendsChanged(accepted) }) } }
            else if (activeSection == "SENT") { if (loadingRequests) item { Text("Dang tai...", color = Muted, modifier = Modifier.padding(24.dp)) }; items(sent, key = { it.requestId }) { request -> SentRequestRow(request, api, onChanged = { loadRequests() }) } }
            else items(friends) { person -> FriendRow(person, presenceByUser[person.id]) }
        }
    }
}

@Composable
private fun ReceivedRequestRow(request: ApiFriendRequest, api: ApiClient, onChanged: (ApiFriend?) -> Unit) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        RemoteAvatar(request.userName, request.avatarUrl, SoftBrand, 46.dp); Spacer(Modifier.width(10.dp)); Text(request.userName, color = Ink, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        TextButton(onClick = { scope.launch { runCatching { api.acceptFriendRequest(request.requestId) }.onSuccess { onChanged(ApiFriend(request.userId, request.userName, request.avatarUrl, null)) } } }) { Text("Nhan", color = Brand) }
        TextButton(onClick = { scope.launch { runCatching { api.rejectFriendRequest(request.requestId) }.onSuccess { onChanged(null) } } }) { Text("Tu choi", color = Color(0xFFB3261E)) }
    }
}

@Composable
private fun SentRequestRow(request: ApiFriendRequest, api: ApiClient, onChanged: () -> Unit) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        RemoteAvatar(request.userName, request.avatarUrl, SoftBrand, 46.dp); Spacer(Modifier.width(10.dp)); Text(request.userName, color = Ink, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); TextButton(onClick = { scope.launch { runCatching { api.cancelFriendRequest(request.requestId) }.onSuccess { onChanged() } } }) { Text("Huy loi moi", color = Color(0xFFB3261E)) }
    }
}

@Composable
private fun FriendRow(friend: ApiFriend, presence: ApiPresence?) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box {
            RemoteAvatar(friend.name, friend.avatarUrl, SoftBrand, 50.dp)
            if (presenceIsOnline(presence)) Box(Modifier.size(13.dp).align(Alignment.BottomEnd).clip(CircleShape).background(Color(0xFF22A06B)).border(2.dp, Background, CircleShape))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(friend.name, color = Ink, fontWeight = FontWeight.Bold)
            Text(friend.bio ?: "Bạn bè", color = Muted, fontSize = 12.sp)
            presenceActivityLabel(presence)?.let { Text(it, color = if (presenceIsOnline(presence)) Brand else Muted, fontSize = 11.sp) }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Đã kết bạn", color = Brand, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SearchUserRow(user: ApiSearchUser, api: ApiClient, onOpenUserProfile: (Long) -> Unit) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var status by remember(user.id) { mutableStateOf<ApiFriendStatus?>(null) }
    var busy by remember(user.id) { mutableStateOf(false) }
    LaunchedEffect(user.id) { runCatching { api.friendStatus(user.id) }.onSuccess { status = it } }
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        RemoteAvatar(user.name, user.avatarUrl, SoftBrand, 50.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).clickable { onOpenUserProfile(user.id) }) { Text(user.name, color = Ink, fontWeight = FontWeight.Bold); Text(user.bio ?: "", color = Muted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis) }
        when (status?.status) {
            "FRIEND" -> Text("Bạn bè", color = Brand, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
            "PENDING_SENT" -> TextButton(enabled = !busy, onClick = { status?.friendshipId?.let { requestId -> scope.launch { busy = true; runCatching { api.cancelFriendRequest(requestId) }.onSuccess { status = ApiFriendStatus(user.id, "NONE", null) }; busy = false } } }) { Text("Đã gửi", color = Muted) }
            "PENDING_RECEIVED" -> Text("Đang chờ bạn", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
            else -> Button(enabled = !busy, onClick = { scope.launch { busy = true; runCatching { api.sendFriendRequest(user.id) }.onSuccess { requestId -> status = ApiFriendStatus(user.id, "PENDING_SENT", requestId) }; busy = false } }, colors = ButtonDefaults.buttonColors(SoftBrand), shape = RoundedCornerShape(12.dp)) { Text(if (busy) "..." else "Thêm", color = Brand) }
        }
    }
}

@Composable
private fun NotificationsScreen(
    notifications: List<ApiNotification>,
    onMarkAllRead: () -> Unit,
    onMarkRead: (Long) -> Unit,
    onDestination: (ApiNotification) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    page: Int,
    totalPages: Int,
    loadingMore: Boolean,
    loadError: String?,
    realtimeConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        AppHeader("Thông báo") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(if (realtimeConnected) Color(0xFF22A06B) else Color(0xFFF59E0B)))
                Spacer(Modifier.width(5.dp))
                Text(if (realtimeConnected) "Đang cập nhật" else "Đang kết nối", color = Muted, fontSize = 11.sp)
                if (notifications.any { !it.read }) {
                    TextButton(onClick = onMarkAllRead) { Text("Đánh dấu đã đọc", color = Brand, fontSize = 12.sp) }
                }
            }
        }
        LazyColumn(Modifier.weight(1f)) {
            if (notifications.isEmpty()) {
                item {
                    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Notifications, null, tint = Muted, modifier = Modifier.size(42.dp))
                        Spacer(Modifier.height(10.dp))
                        Text("Chưa có thông báo", color = Muted)
                    }
                }
            } else {
                items(notifications, key = { it.id ?: it.hashCode().toLong() }) { notification ->
                    NotificationRow(notification, onClick = {
                        onDestination(notification)
                        if (!notification.read && notification.id != null) onMarkRead(notification.id)
                    })
                    Divider(color = Color(0xFFE5E7EB), thickness = 0.6.dp)
                }
            }
            if (!loadError.isNullOrBlank()) {
                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(loadError, color = Color(0xFFB42318), fontSize = 12.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = onRetry) { Text("Thử lại", color = Brand, fontSize = 12.sp) }
                    }
                }
            }
            if (notifications.isNotEmpty() && page + 1 < totalPages) {
                item {
                    TextButton(enabled = !loadingMore, onClick = onLoadMore, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        if (loadingMore) CircularProgressIndicator(color = Brand, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        else Text("Tải thêm thông báo", color = Brand)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: ApiNotification, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).background(if (notification.read) Color.Transparent else SoftBrand.copy(alpha = 0.48f)).padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp)) {
            if (notification.actorAvatarUrl.isNullOrBlank()) {
                Box(Modifier.fillMaxSize().clip(CircleShape).background(SoftBrand), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Person, null, tint = Brand) }
            } else {
                RemoteAvatar(notification.actorName ?: "N", notification.actorAvatarUrl, SoftBrand, 44.dp)
            }
            Box(Modifier.align(Alignment.BottomEnd).size(19.dp).clip(CircleShape).background(notificationIconColor(notification)), contentAlignment = Alignment.Center) {
                Icon(notificationIcon(notification), null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(notification.text, color = Ink, fontSize = 14.sp, fontWeight = if (notification.read) FontWeight.Normal else FontWeight.SemiBold)
            val detail = listOfNotNull(notification.actorName, notificationTypeLabel(notification.type, notification.reactionType), notification.createdAt?.let(::formatPostTime)).joinToString(" · ")
            Text(detail.ifBlank { "Thông báo mới" }, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (!notification.read) Box(Modifier.size(8.dp).clip(CircleShape).background(Brand))
    }
}

private fun notificationIcon(notification: ApiNotification): androidx.compose.ui.graphics.vector.ImageVector = when (notification.type.uppercase()) {
    "FRIEND_REQUEST", "FRIEND_ACCEPTED" -> Icons.Filled.Person
    "POST_REACTION", "POST_LIKE", "STORY_REACTION" -> if (notification.reactionType.equals("LOVE", true)) Icons.Filled.FavoriteBorder else Icons.Filled.ThumbUp
    "POST_COMMENT", "COMMENT_REPLY" -> Icons.Filled.ChatBubble
    "POST_SHARE" -> Icons.Filled.Share
    "SECURITY_LOGIN" -> Icons.Filled.Lock
    "NEW_MESSAGE", "MESSAGE" -> Icons.Filled.ChatBubbleOutline
    else -> Icons.Filled.Notifications
}

private fun notificationIconColor(notification: ApiNotification): Color = when (notification.type.uppercase()) {
    "POST_REACTION", "POST_LIKE", "STORY_REACTION" -> if (notification.reactionType.equals("LOVE", true)) Color(0xFFE53935) else Brand
    "SECURITY_LOGIN" -> Color(0xFF8E5CF6)
    else -> Brand
}

private fun notificationTypeLabel(type: String, reactionType: String? = null): String = when (type.uppercase()) {
    "FRIEND_REQUEST" -> "Lời mời kết bạn"
    "FRIEND_ACCEPTED" -> "Đã chấp nhận kết bạn"
    "POST_REACTION" -> "${reactionTypeLabel(reactionType)} bài viết"
    "COMMENT", "POST_COMMENT" -> "Bình luận"
    "REPLY", "COMMENT_REPLY" -> "Phản hồi bình luận"
    "SHARE", "POST_SHARE" -> "Chia sẻ"
    "MENTION" -> "Được nhắc đến"
    "STORY_REACTION" -> "${reactionTypeLabel(reactionType)} tin"
    "SECURITY_LOGIN" -> "Bảo mật tài khoản"
    "NEW_MESSAGE", "MESSAGE" -> "Tin nhắn mới"
    else -> type.lowercase().replace('_', ' ').ifBlank { "Thông báo mới" }
}

private fun reactionTypeLabel(type: String?): String = when (type?.uppercase()) {
    "LIKE" -> "Đã thích"
    "LOVE" -> "Đã yêu thích"
    "HAHA" -> "Đã thả Haha"
    "WOW" -> "Đã thả Wow"
    "SAD" -> "Đã thả Buồn"
    "ANGRY" -> "Đã thả Phẫn nộ"
    else -> "Tương tác"
}

@Composable
private fun UserPostsScreen(api: ApiClient, userId: Long, onBack: () -> Unit) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    LaunchedEffect(userId) { runCatching { api.userPosts(userId).map { it.toPost() } }.onSuccess { posts = it }.onFailure { error = it.message } }
    Column(Modifier.fillMaxSize()) {
        AppHeader("Bai viet cua toi") { TextButton(onClick = onBack) { Text("Quay lai", color = Brand) } }
        error?.let { Text(it, color = Color(0xFFB3261E), modifier = Modifier.padding(18.dp)) }
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 20.dp)) {
            if (posts.isEmpty()) item { Text("Chua co bai viet", color = Muted, modifier = Modifier.padding(24.dp)) }
            items(posts, key = { it.id ?: it.hashCode().toLong() }) { post -> PostCard(post, api, onChanged = { scope.launch { runCatching { posts = api.userPosts(userId).map { it.toPost() } } } }) }
        }
    }
}

@Composable
private fun SavedPostsScreen(api: ApiClient, onBack: () -> Unit) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var page by remember { mutableStateOf(0) }
    var totalPages by remember { mutableStateOf(0) }
    var totalItems by remember { mutableStateOf(0L) }
    var loading by remember { mutableStateOf(true) }
    var loadingMore by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun load(firstPage: Boolean = true) {
        scope.launch {
            if (firstPage) loading = true else loadingMore = true
            error = null
            runCatching { api.savedPostsPage(if (firstPage) 0 else page + 1) }
                .onSuccess { result ->
                    val mapped = result.items.map { it.toPost() }
                    posts = if (firstPage) mapped else (posts + mapped).distinctBy { it.id }
                    page = result.page
                    totalPages = result.totalPages
                    totalItems = result.totalItems
                }
                .onFailure { error = it.message ?: "Khong the tai bai viet da luu" }
            if (firstPage) loading = false else loadingMore = false
        }
    }
    LaunchedEffect(Unit) { load() }
    Column(Modifier.fillMaxSize()) {
        AppHeader("Bai viet da luu") { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Quay lai", tint = Ink) } }
        Text("$totalItems bai viet", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp))
        error?.let { Text(it, color = Color(0xFFB3261E), modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) }
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 20.dp)) {
            if (loading) item { Text("Dang tai...", color = Muted, modifier = Modifier.padding(24.dp)) }
            else if (posts.isEmpty()) item { Text("Chua co bai viet da luu", color = Muted, modifier = Modifier.padding(24.dp)) }
            items(posts, key = { it.id ?: it.hashCode().toLong() }) { post ->
                PostCard(post, api, saved = true, onChanged = { load() })
            }
            if (!loading && page + 1 < totalPages) item {
                TextButton(enabled = !loadingMore, onClick = { load(false) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (loadingMore) "Dang tai..." else "Tai them", color = Brand)
                }
            }
        }
    }
}

@Composable
private fun MyReportsScreen(api: ApiClient, onBack: () -> Unit) {
    var reports by remember { mutableStateOf<List<ApiReport>>(emptyList()) }
    var page by remember { mutableStateOf(0) }
    var totalPages by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var loadingMore by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun load(firstPage: Boolean = true) {
        scope.launch {
            if (firstPage) loading = true else loadingMore = true
            runCatching { api.myReportsPage(if (firstPage) 0 else page + 1) }
                .onSuccess { result ->
                    reports = if (firstPage) result.items else (reports + result.items).distinctBy { it.id }
                    page = result.page
                    totalPages = result.totalPages
                    error = null
                }
                .onFailure { error = it.message ?: "Khong the tai bao cao" }
            if (firstPage) loading = false else loadingMore = false
        }
    }
    LaunchedEffect(Unit) { load() }
    Column(Modifier.fillMaxSize()) {
        AppHeader("Bao cao cua toi") { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Quay lai", tint = Ink) } }
        error?.let { Text(it, color = Color(0xFFB3261E), modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) }
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (loading) item { Text("Dang tai...", color = Muted, modifier = Modifier.padding(18.dp)) }
            else if (reports.isEmpty()) item { Text("Ban chua gui bao cao nao", color = Muted, modifier = Modifier.padding(18.dp)) }
            items(reports, key = { it.id ?: it.hashCode().toLong() }) { report ->
                Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(reportTargetLabel(report.targetType), color = Ink, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(reportStatusLabel(report.status), color = reportStatusColor(report.status), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                        Text("Ly do: ${reportReasonLabel(report.reason)}", color = Muted, fontSize = 13.sp)
                        report.description?.takeIf { it.isNotBlank() }?.let { Text(it, color = Ink, fontSize = 13.sp, maxLines = 3, overflow = TextOverflow.Ellipsis) }
                        Text(formatPostTime(report.createdAt.orEmpty()), color = Muted, fontSize = 11.sp)
                    }
                }
            }
            if (!loading && page + 1 < totalPages) item {
                TextButton(enabled = !loadingMore, onClick = { load(false) }, modifier = Modifier.fillMaxWidth()) { Text(if (loadingMore) "Dang tai..." else "Tai them", color = Brand) }
            }
        }
    }
}

private fun reportTargetLabel(type: String): String = when (type.uppercase()) {
    "POST" -> "Bao cao bai viet"
    "COMMENT" -> "Bao cao binh luan"
    "USER" -> "Bao cao nguoi dung"
    else -> "Bao cao"
}

private fun reportReasonLabel(reason: String): String = when (reason.uppercase()) {
    "SPAM" -> "Spam"
    "HARASSMENT" -> "Quay roi"
    "HATE_SPEECH" -> "Ngon tu thu ghet"
    "VIOLENCE" -> "Bao luc"
    "SEXUAL_CONTENT" -> "Noi dung nhay cam"
    "SCAM" -> "Lua dao"
    "FALSE_INFORMATION" -> "Thong tin sai"
    else -> "Ly do khac"
}

private fun reportStatusLabel(status: String): String = when (status.uppercase()) {
    "PENDING" -> "Dang cho"
    "REVIEWING" -> "Dang xem xet"
    "RESOLVED" -> "Da xu ly"
    "REJECTED" -> "Tu choi"
    else -> status
}

private fun reportStatusColor(status: String): Color = when (status.uppercase()) {
    "RESOLVED" -> Color(0xFF16803C)
    "REJECTED" -> Color(0xFFB42318)
    "REVIEWING" -> Color(0xFFB54708)
    else -> Brand
}

@Composable
private fun ProfileIdentityHeader(
    profile: ApiProfile?,
    coverAction: (@Composable () -> Unit)?,
    stats: (@Composable () -> Unit)?,
    avatarAction: (@Composable () -> Unit)? = null,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .height(240.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFDDEAF7))
                    .align(Alignment.TopCenter)
            ) {
                profile?.coverUrl?.takeIf { it.isNotBlank() }?.let { RemoteImage(it, Modifier.fillMaxSize()) }
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.08f)))
                coverAction?.let { Box(Modifier.align(Alignment.BottomEnd).padding(10.dp)) { it() } }
            }
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.size(100.dp).align(Alignment.BottomStart)
            ) {
                Box(Modifier.padding(4.dp)) {
                    RemoteAvatar(profile?.userName ?: "U", profile?.avatarUrl, SoftBrand, 92.dp)
                }
            }
            avatarAction?.let { Box(Modifier.align(Alignment.BottomStart).offset(x = 70.dp, y = (-2).dp)) { it() } }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
            Text(
                profile?.userName ?: "Người dùng",
                color = Ink,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                profile?.bio?.takeIf { it.isNotBlank() } ?: "Chưa thêm phần giới thiệu",
                color = Muted,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
            stats?.let {
                Divider(Modifier.padding(top = 14.dp), color = Color(0xFFE5E7EB))
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) { it() }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Brand, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

@Composable
private fun ProfileFact(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Muted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, color = Ink, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ProfileActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(SoftBrand),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = Brand, modifier = Modifier.size(19.dp)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Filled.ArrowForward, null, tint = Muted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ProfileSectionHeader(title: String, trailing: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(trailing, color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun PublicProfileScreen(
    userId: Long,
    api: ApiClient,
    onBack: () -> Unit
) {
    var profile by remember(userId) { mutableStateOf<ApiProfile?>(null) }
    var relation by remember(userId) { mutableStateOf<ApiFriendStatus?>(null) }
    var posts by remember(userId) { mutableStateOf<List<Post>>(emptyList()) }
    var postsPage by remember(userId) { mutableStateOf(0) }
    var postsTotalPages by remember(userId) { mutableStateOf(0) }
    var postsTotalItems by remember(userId) { mutableStateOf(0L) }
    var loadingMorePosts by remember(userId) { mutableStateOf(false) }
    var loading by remember(userId) { mutableStateOf(true) }
    var busy by remember(userId) { mutableStateOf(false) }
    var error by remember(userId) { mutableStateOf<String?>(null) }
    var showReport by remember(userId) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            loading = true
            error = null
            runCatching {
                val loadedProfile = api.userProfile(userId)
                val loadedRelation = api.friendStatus(userId)
                val loadedPostPage = api.userPostsPage(userId)
                Triple(loadedProfile, loadedRelation, loadedPostPage)
            }.onSuccess { (loadedProfile, loadedRelation, loadedPostPage) ->
                profile = loadedProfile
                relation = loadedRelation
                posts = loadedPostPage.items.map { it.toPost() }
                postsPage = loadedPostPage.page
                postsTotalPages = loadedPostPage.totalPages
                postsTotalItems = loadedPostPage.totalItems
            }.onFailure { error = it.message ?: "Khong the tai ho so" }
            loading = false
        }
    }
    fun loadMorePosts() {
        if (loading || loadingMorePosts || postsPage + 1 >= postsTotalPages) return
        scope.launch {
            loadingMorePosts = true
            runCatching { api.userPostsPage(userId, postsPage + 1) }
                .onSuccess { page -> posts = (posts + page.items.map { it.toPost() }).distinctBy { it.id }; postsPage = page.page; postsTotalPages = page.totalPages; postsTotalItems = page.totalItems }
                .onFailure { error = it.message ?: "Khong the tai them bai viet" }
            loadingMorePosts = false
        }
    }
    LaunchedEffect(userId) { reload() }

    Column(Modifier.fillMaxSize().background(Background)) {
        AppHeader(profile?.userName ?: "Hồ sơ") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showReport = true }) { Icon(Icons.Filled.Flag, "Báo cáo người dùng", tint = Muted) }
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Quay lại", tint = Ink) }
            }
        }
        if (loading && profile == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Brand) }
        } else if (error != null && profile == null) {
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(error.orEmpty(), color = Color(0xFFB3261E))
                TextButton(onClick = ::reload) { Text("Thử lại", color = Brand) }
            }
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ProfileIdentityHeader(
                        profile = profile,
                        coverAction = null,
                        stats = { ProfileStat(postsTotalItems.toString(), "bài viết") },
                        actions = {
                            when (relation?.status) {
                                "FRIEND" -> OutlinedButton(
                                    enabled = !busy,
                                    onClick = {
                                        scope.launch {
                                            busy = true
                                            runCatching { api.removeFriend(userId) }
                                                .onSuccess { relation = ApiFriendStatus(userId, "NONE", null) }
                                            busy = false
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.People, null, Modifier.size(17.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Bạn bè", color = Brand)
                                }
                                "PENDING_SENT" -> OutlinedButton(
                                    enabled = !busy,
                                    onClick = {
                                        relation?.friendshipId?.let { requestId ->
                                            scope.launch {
                                                busy = true
                                                runCatching { api.cancelFriendRequest(requestId) }
                                                    .onSuccess { relation = ApiFriendStatus(userId, "NONE", null) }
                                                busy = false
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Đã gửi lời mời", color = Muted)
                                }
                                "PENDING_RECEIVED" -> OutlinedButton(enabled = false, onClick = {}, shape = RoundedCornerShape(10.dp)) {
                                    Text("Lời mời đang chờ", color = Muted)
                                }
                                "SELF" -> Unit
                                else -> Button(
                                    enabled = !busy,
                                    onClick = {
                                        scope.launch {
                                            busy = true
                                            runCatching { api.sendFriendRequest(userId) }
                                                .onSuccess { requestId -> relation = ApiFriendStatus(userId, "PENDING_SENT", requestId) }
                                            busy = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Brand),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Person, null, Modifier.size(17.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (busy) "Đang xử lý" else "Thêm bạn bè")
                                }
                            }
                        }
                    )
                }
                item {
                    ProfileSection(
                        title = "Giới thiệu",
                        icon = Icons.Filled.Info
                    ) {
                        profile?.email?.takeIf { it.isNotBlank() }?.let { ProfileFact(Icons.Filled.Email, it) }
                        profile?.gender?.takeIf { it.isNotBlank() }?.let { ProfileFact(Icons.Filled.Person, "Giới tính: $it") }
                        profile?.dateOfBirth?.takeIf { it.isNotBlank() }?.let { ProfileFact(Icons.Filled.CalendarToday, "Ngày sinh: $it") }
                        if (profile?.email.isNullOrBlank() && profile?.gender.isNullOrBlank() && profile?.dateOfBirth.isNullOrBlank()) {
                            Text("Chưa có thông tin giới thiệu", color = Muted, fontSize = 13.sp)
                        }
                    }
                }
                item {
                    ProfileSectionHeader("Bài viết", "$postsTotalItems bài viết")
                }
                if (!error.isNullOrBlank()) item { Text(error.orEmpty(), color = Color(0xFFB3261E), modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)) }
                if (posts.isEmpty() && !loading) item {
                    Surface(color = Color.White, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
                        Text("Chưa có bài viết", color = Muted, modifier = Modifier.padding(22.dp))
                    }
                }
                items(posts, key = { it.id ?: it.hashCode().toLong() }) { post ->
                    PostCard(post, api, onChanged = { reload() })
                }
                if (postsPage + 1 < postsTotalPages) item {
                    TextButton(enabled = !loadingMorePosts, onClick = ::loadMorePosts, modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
                        if (loadingMorePosts) CircularProgressIndicator(Modifier.size(16.dp), color = Brand, strokeWidth = 2.dp)
                        else Text("Tải thêm bài viết", color = Brand)
                    }
                }
            }
        }
    }
    if (showReport) ReportDialog(
        title = "Báo cáo người dùng",
        onDismiss = { showReport = false },
        onSubmit = { reason, description ->
            scope.launch {
                runCatching { api.reportUser(userId, reason, description) }
                    .onSuccess { showReport = false; error = "Đã gửi báo cáo" }
                    .onFailure { error = it.message ?: "Không thể gửi báo cáo" }
            }
        }
    )
}

@Composable
private fun ProfileScreen(profile: ApiProfile?, api: ApiClient, onProfileUpdated: (ApiProfile) -> Unit, onTrustDevice: () -> Unit, onOpenSecurity: () -> Unit, onOpenMyPosts: () -> Unit, onOpenSavedPosts: () -> Unit, onOpenMyReports: () -> Unit, isAdmin: Boolean, onOpenAdmin: () -> Unit, onLogout: () -> Unit, modifier: Modifier = Modifier) {
    var showEdit by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var imageBusy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val context = LocalContext.current
    fun uploadProfileImage(uri: Uri, cover: Boolean) {
        val current = profile ?: return
        scope.launch {
            imageBusy = true
            error = null
            runCatching {
                val resolver = context.contentResolver
                val mime = resolver.getType(uri) ?: "image/*"
                val bytes = readValidatedUpload(context, uri, mime)
                val upload = api.uploadMedia("profile-${System.currentTimeMillis()}.jpg", mime, bytes)
                val imageUrl = upload.url?.takeIf { it.isNotBlank() } ?: error("Backend khong tra ve URL anh")
                api.updateProfile(current.userName, current.bio, if (cover) current.avatarUrl else imageUrl, if (cover) imageUrl else current.coverUrl, current.dateOfBirth, current.gender)
            }.onSuccess(onProfileUpdated).onFailure { error = it.message ?: "Khong the cap nhat anh" }
            imageBusy = false
        }
    }
    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { uploadProfileImage(it, false) } }
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { uploadProfileImage(it, true) } }
    Column(modifier.fillMaxSize().background(Background)) {
        AppHeader("Trang cá nhân") { IconButton(onClick = {}) { Icon(Icons.Filled.MoreHoriz, "Tùy chọn", tint = Ink) } }
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ProfileIdentityHeader(
                    profile = profile,
                    coverAction = {
                        IconButton(
                            enabled = !imageBusy,
                            onClick = { coverPicker.launch("image/*") },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.42f), CircleShape)
                        ) { Icon(Icons.Filled.CameraAlt, "Đổi ảnh bìa", tint = Color.White) }
                    },
                    stats = null,
                    avatarAction = {
                        IconButton(
                            enabled = !imageBusy,
                            onClick = { avatarPicker.launch("image/*") },
                            modifier = Modifier.size(32.dp).background(Brand, CircleShape)
                        ) { Icon(Icons.Filled.CameraAlt, "Đổi ảnh đại diện", tint = Color.White, modifier = Modifier.size(16.dp)) }
                    },
                    actions = {
                        Button(
                            onClick = { error = null; showEdit = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Brand),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.Edit, null, Modifier.size(17.dp))
                            Spacer(Modifier.width(7.dp))
                            Text("Chỉnh sửa trang cá nhân")
                        }
                        if (imageBusy) CircularProgressIndicator(Modifier.size(20.dp), color = Brand, strokeWidth = 2.dp)
                    }
                )
            }
            item {
                ProfileSection(title = "Thông tin cá nhân", icon = Icons.Filled.Info) {
                    profile?.email?.takeIf { it.isNotBlank() }?.let { ProfileFact(Icons.Filled.Email, it) }
                    profile?.gender?.takeIf { it.isNotBlank() }?.let { ProfileFact(Icons.Filled.Person, "Giới tính: $it") }
                    profile?.dateOfBirth?.takeIf { it.isNotBlank() }?.let { ProfileFact(Icons.Filled.CalendarToday, "Ngày sinh: $it") }
                    if (profile?.email.isNullOrBlank() && profile?.gender.isNullOrBlank() && profile?.dateOfBirth.isNullOrBlank()) {
                        Text("Thêm thông tin để bạn bè hiểu bạn hơn", color = Muted, fontSize = 13.sp)
                    }
                }
            }
            item {
                ProfileSection(title = "Không gian của bạn", icon = Icons.Filled.GridView) {
                    ProfileActionRow(Icons.Filled.PhotoLibrary, "Bài viết của tôi", "Xem lại những gì bạn đã chia sẻ", onOpenMyPosts)
                    ProfileActionRow(Icons.Filled.BookmarkBorder, "Bài viết đã lưu", "Nội dung bạn muốn xem lại", onOpenSavedPosts)
                    ProfileActionRow(Icons.Filled.Flag, "Báo cáo của tôi", "Theo dõi các báo cáo đã gửi", onOpenMyReports)
                }
            }
            item {
                ProfileSection(title = "Bảo mật và quyền truy cập", icon = Icons.Filled.Lock) {
                    ProfileActionRow(Icons.Filled.Lock, "Thiết bị tin cậy", "Xác nhận thiết bị này", onTrustDevice)
                    ProfileActionRow(Icons.Filled.Lock, "Thiết bị và phiên đăng nhập", "Quản lý các phiên đang hoạt động", onOpenSecurity)
                    if (isAdmin) ProfileActionRow(Icons.Filled.AdminPanelSettings, "Trung tâm quản trị", "Quản lý báo cáo và hoạt động", onOpenAdmin)
                }
            }
            item {
                TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
                    Text("Đăng xuất", color = Color(0xFFB3261E), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
    if (showEdit && profile != null) EditProfileDialog(profile, saving, error, onDismiss = { if (!saving) showEdit = false }, onSave = { userName, bio, dateOfBirth, gender ->
        scope.launch {
            saving = true; error = null
            runCatching { api.updateProfile(userName, bio, profile.avatarUrl, profile.coverUrl, dateOfBirth, gender) }
                .onSuccess { updated -> onProfileUpdated(updated); showEdit = false }
                .onFailure { error = it.message ?: "Không thể cập nhật hồ sơ" }
            saving = false
        }
    })
}

@Composable
private fun EditProfileDialog(profile: ApiProfile, saving: Boolean, error: String?, onDismiss: () -> Unit, onSave: (String, String, String?, String?) -> Unit) {
    var userName by remember(profile) { mutableStateOf(profile.userName) }
    var bio by remember(profile) { mutableStateOf(profile.bio.orEmpty()) }
    var dateOfBirth by remember(profile) { mutableStateOf(profile.dateOfBirth.orEmpty()) }
    var gender by remember(profile) { mutableStateOf(profile.gender.orEmpty()) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Chỉnh sửa hồ sơ") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(userName, { userName = it.take(50) }, label = { Text("Tên người dùng") }, singleLine = true, enabled = !saving)
            OutlinedTextField(bio, { bio = it.take(500) }, label = { Text("Giới thiệu") }, minLines = 2, enabled = !saving)
            OutlinedTextField(dateOfBirth, { dateOfBirth = it.take(10) }, label = { Text("Ngày sinh (yyyy-MM-dd)") }, singleLine = true, enabled = !saving)
            OutlinedTextField(gender, { gender = it.take(20) }, label = { Text("Giới tính") }, singleLine = true, enabled = !saving)
            error?.let { Text(it, color = Color(0xFFB3261E), fontSize = 12.sp) }
        }
    }, confirmButton = { TextButton(enabled = !saving && userName.isNotBlank(), onClick = { onSave(userName.trim(), bio.trim(), dateOfBirth.trim().ifBlank { null }, gender.trim().ifBlank { null }) }) { Text(if (saving) "Đang lưu..." else "Lưu", color = Brand) } }, dismissButton = { TextButton(enabled = !saving, onClick = onDismiss) { Text("Hủy") } })
}

@Composable
private fun SecuritySettingsScreen(api: ApiClient, onBack: () -> Unit, onLogout: () -> Unit) {
    var sessions by remember { mutableStateOf<List<ApiSecuritySession>>(emptyList()) }
    var approvals by remember { mutableStateOf<List<ApiPendingLoginApproval>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    var confirmOthers by remember { mutableStateOf(false) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    fun load() {
        scope.launch {
            loading = true; error = null
            runCatching { api.securitySessions() to api.pendingLoginApprovals() }.onSuccess { (loadedSessions, loadedApprovals) -> sessions = loadedSessions; approvals = loadedApprovals }.onFailure { error = it.message ?: "Không thể tải dữ liệu bảo mật" }
            loading = false
        }
    }
    LaunchedEffect(Unit) { load() }
    val active = sessions.count { it.status == "ACTIVE" }
    val trusted = sessions.filter { it.trusted }.mapNotNull { it.userDeviceId }.distinct().size
    Column(Modifier.fillMaxSize()) {
        AppHeader("Bảo mật đăng nhập") { TextButton(onClick = onBack) { Text("Quay lại", color = Brand) } }
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Thiết bị và phiên đăng nhập", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("$active đang hoạt động  ·  $trusted thiết bị tin cậy", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 14.dp)) {
                            OutlinedButton(onClick = { load() }, enabled = !loading, shape = RoundedCornerShape(12.dp)) { Text("Tải lại") }
                            Button(onClick = { confirmOthers = true }, enabled = !loading && active > 1, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E)), shape = RoundedCornerShape(12.dp)) { Text("Đăng xuất thiết bị khác") }
                        }
                    }
                }
                notice?.let { Text(it, color = Brand, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 4.dp)) }
                error?.let { Text(it, color = Color(0xFFB3261E), fontSize = 13.sp, modifier = Modifier.padding(horizontal = 4.dp)) }
            }
            if (loading) item { Text("Đang tải phiên đăng nhập...", color = Muted, modifier = Modifier.padding(18.dp)) }
            else {
                if (approvals.isNotEmpty()) {
                    item { Text("Yêu cầu đăng nhập đang chờ", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }
                    items(approvals, key = { "approval-${it.id}" }) { approval ->
                        PendingApprovalCard(approval, busy == "approval:${approval.id}", onApprove = {
                            scope.launch { busy = "approval:${approval.id}"; runCatching { api.approveLogin(approval.id) }.onSuccess { notice = "Đã chấp thuận yêu cầu đăng nhập."; load() }.onFailure { error = it.message }; busy = null }
                        }, onReject = {
                            scope.launch { busy = "approval:${approval.id}"; runCatching { api.rejectLogin(approval.id) }.onSuccess { notice = "Đã từ chối yêu cầu đăng nhập."; load() }.onFailure { error = it.message }; busy = null }
                        })
                    }
                }
                if (sessions.isEmpty()) item { Text("Chưa có dữ liệu thiết bị.", color = Muted, modifier = Modifier.padding(18.dp)) }
                else items(sessions, key = { it.id }) { device ->
                    SecuritySessionCard(device, busy == "session:${device.id}" || busy == "device:${device.userDeviceId}", onTrust = {
                        val id = device.userDeviceId ?: return@SecuritySessionCard
                        scope.launch { busy = "device:$id"; runCatching { if (device.trusted) api.untrustDevice(id) else api.trustDevice(id) }.onSuccess { notice = if (device.trusted) "Đã bỏ tin cậy thiết bị." else "Đã tin cậy thiết bị."; load() }.onFailure { error = it.message }; busy = null }
                    }, onRevoke = {
                        scope.launch { busy = "session:${device.id}"; runCatching { api.revokeSession(device.id) }.onSuccess { notice = "Đã thu hồi phiên đăng nhập."; if (device.current) onLogout() else load() }.onFailure { error = it.message }; busy = null }
                    })
                }
            }
        }
    }
    if (confirmOthers) AlertDialog(onDismissRequest = { confirmOthers = false }, title = { Text("Đăng xuất thiết bị khác?") }, text = { Text("Tất cả phiên khác sẽ bị thu hồi và cần đăng nhập lại.") }, confirmButton = { TextButton(onClick = { confirmOthers = false; scope.launch { busy = "others"; runCatching { api.revokeOtherSessions() }.onSuccess { notice = "Đã đăng xuất các thiết bị khác."; load() }.onFailure { error = it.message }; busy = null } }) { Text("Xác nhận", color = Color(0xFFB3261E)) } }, dismissButton = { TextButton(onClick = { confirmOthers = false }) { Text("Hủy") } })
}

@Composable
private fun PendingApprovalCard(approval: ApiPendingLoginApproval, busy: Boolean, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(colors = CardDefaults.cardColors(Color(0xFFFFFBEB)), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar("!", Color(0xFFFDE68A), 44.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(approval.deviceName ?: "Thiết bị mới", color = Ink, fontWeight = FontWeight.Bold)
                    Text("${approval.os ?: "OS không xác định"} · ${approval.deviceType ?: "Thiết bị"}", color = Muted, fontSize = 12.sp)
                }
                Text("Rủi ro ${approval.riskScore}", color = Color(0xFFB45309), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text("IP: ${approval.ipAddress ?: "Không xác định"}", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
            Text("Yêu cầu lúc: ${formatPostTime(approval.createdAt.orEmpty())}", color = Muted, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                Button(onClick = onApprove, enabled = !busy, colors = ButtonDefaults.buttonColors(containerColor = Brand), shape = RoundedCornerShape(11.dp)) { Text("Chấp thuận") }
                OutlinedButton(onClick = onReject, enabled = !busy, shape = RoundedCornerShape(11.dp)) { Text("Từ chối", color = Color(0xFFB3261E)) }
            }
        }
    }
}

@Composable
private fun SecuritySessionCard(session: ApiSecuritySession, busy: Boolean, onTrust: () -> Unit, onRevoke: () -> Unit) {
    Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(session.deviceName?.take(1) ?: "D", if (session.current) SoftBrand else Color(0xFFEFF1F4), 46.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(session.deviceName ?: "Thiết bị không xác định", color = Ink, fontWeight = FontWeight.Bold)
                    Text(listOfNotNull(session.os, session.browser, session.deviceType).joinToString(" · ").ifBlank { "Thiết bị đăng nhập" }, color = Muted, fontSize = 12.sp)
                }
                if (session.current) Text("Hiện tại", color = Brand, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Text("IP: ${session.ipAddress ?: "Không xác định"}", color = Muted, fontSize = 12.sp)
            Text("Hoạt động gần nhất: ${formatPostTime(session.lastActiveAt.orEmpty())}", color = Muted, fontSize = 12.sp)
            Text(if (session.trusted) "Thiết bị tin cậy" else "Chưa tin cậy", color = if (session.trusted) Brand else Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                OutlinedButton(onClick = onTrust, enabled = !busy && session.userDeviceId != null, shape = RoundedCornerShape(11.dp)) { Text(if (session.trusted) "Bỏ tin cậy" else "Tin cậy") }
                OutlinedButton(onClick = onRevoke, enabled = !busy && session.status == "ACTIVE", shape = RoundedCornerShape(11.dp)) { Text(if (session.current) "Đăng xuất phiên này" else "Thu hồi phiên") }
            }
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp); Text(label, color = Muted, fontSize = 12.sp) }
}

@Composable
private fun Avatar(letter: String, color: Color, size: androidx.compose.ui.unit.Dp = 42.dp) {
    Box(Modifier.size(size).clip(CircleShape).background(color), contentAlignment = Alignment.Center) { Text(letter, color = Ink, fontWeight = FontWeight.Bold, fontSize = (size.value / 2.4f).sp) }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SocialMobilePreview() { LoginScreen(onLogin = { _, _ -> }, onRegister = { _, _, _ -> }, error = null) }
