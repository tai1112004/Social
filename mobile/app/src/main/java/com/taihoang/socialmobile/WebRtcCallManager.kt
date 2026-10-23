package com.taihoang.socialmobile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RTCStats
import org.webrtc.RTCStatsCollectorCallback
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import org.webrtc.audio.JavaAudioDeviceModule
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resumeWithException

/**
 * Owns one 1:1 WebRTC connection. Signaling remains in StompClient.
 *
 * Threading: the PeerConnection, local media and the remote-candidate queue are only touched on
 * [webRtcThread]; public methods just queue work there. Many PeerConnection calls block until
 * WebRTC's signaling thread runs them, and that thread also delivers callbacks, so no lock may be
 * held around a WebRTC call and no callback may wait for anything: they only queue work or notify
 * the app. Callback bodies are guarded because an exception escaping into WebRTC aborts the process.
 */
class WebRtcCallManager(context: Context) {
    var onLocalVideoReady: (() -> Unit)? = null
    var onRemoteVideoReady: (() -> Unit)? = null
    var onCameraFacingChanged: ((Boolean) -> Unit)? = null
    var onAudioRouteChanged: ((CallAudioRoute) -> Unit)? = null
    var onCallInterruptionChanged: ((Boolean, String?) -> Unit)? = null
    var onCallQualityChanged: ((CallQuality) -> Unit)? = null
    var onConnectionStateChanged: ((String) -> Unit)? = null
    var onIceCandidateReady: ((IceCandidate) -> Unit)? = null
    var onFailure: ((String) -> Unit)? = null

    private val appContext = context.applicationContext
    private val webRtcThread: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { runnable -> Thread(runnable, "social-webrtc") }
    private val eglBase: EglBase
    private val audioDeviceModule: JavaAudioDeviceModule
    private val factory: PeerConnectionFactory

    @Volatile
    private var disposed = false

    /** Bumped whenever the connection is replaced or closed; callbacks of older ones are dropped. */
    @Volatile
    private var generation = 0L

    // Owned by webRtcThread.
    private var peerConnection: PeerConnection? = null
    private var media: LocalMedia? = null
    private var remoteDescriptionSet = false
    private val pendingRemoteCandidates = mutableListOf<IceCandidate>()
    private var statsTask: ScheduledFuture<*>? = null
    private var lastCallQuality: CallQuality? = null

    // Read by the UI thread.
    @Volatile
    private var localVideoTrack: VideoTrack? = null
    @Volatile
    private var remoteVideoTrack: VideoTrack? = null

    // Written by the caller, applied to the tracks on webRtcThread.
    @Volatile
    private var muted = false
    @Volatile
    private var cameraEnabled = true

    // AudioManager is thread-safe; audioLock only guards these flags and never surrounds a WebRTC call.
    private val audioLock = Any()
    private var audioRoute = CallAudioRoute.EARPIECE
    private var audioSessionActive = false
    private var previousAudioMode = AudioManager.MODE_NORMAL
    private var previousSpeakerphoneOn = false
    private var audioFocusRequest: AudioFocusRequest? = null
    private var audioFocusInterrupted = false
    private var phoneCallActive = false
    @Volatile
    private var mediaInterrupted = false
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val telephonyManager = appContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private class LocalMedia(
        val audioSource: AudioSource,
        val audioTrack: AudioTrack,
        val videoSource: VideoSource? = null,
        val videoTrack: VideoTrack? = null,
        val capturer: CameraVideoCapturer? = null,
        val textureHelper: SurfaceTextureHelper? = null,
        val frontCameraName: String? = null,
        val backCameraName: String? = null,
        var currentCameraName: String? = null
    ) {
        fun dispose() {
            runCatching { capturer?.stopCapture() }
            capturer?.dispose()
            textureHelper?.dispose()
            videoTrack?.dispose()
            videoSource?.dispose()
            audioTrack.dispose()
            audioSource.dispose()
        }
    }

    init {
        initializeWebRtc(appContext)
        eglBase = EglBase.create()
        audioDeviceModule = JavaAudioDeviceModule.builder(appContext)
            .setUseHardwareAcousticEchoCanceler(false)
            .setUseHardwareNoiseSuppressor(false)
            .createAudioDeviceModule()
        factory = PeerConnectionFactory.builder()
            .setAudioDeviceModule(audioDeviceModule)
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()
    }

    val localVideo: VideoTrack?
        get() = localVideoTrack

    val remoteVideo: VideoTrack?
        get() = remoteVideoTrack

    val eglContext: EglBase.Context
        get() = eglBase.eglBaseContext

    /** Opens a new connection with local media; throws if the camera or connection cannot be created. */
    suspend fun prepare(callType: String, iceServers: JSONArray) {
        // On the caller's thread: PhoneStateListener needs the main looper it was created on.
        beginAudioSession()
        val servers = parseIceServers(iceServers)
        onWebRtcThread { openConnection(callType.equals("VIDEO", ignoreCase = true), servers) }
    }

    fun createOffer(onOffer: (SessionDescription) -> Unit) =
        createDescription("createOffer", MediaConstraints(), offer = true, onOffer)

    fun createIceRestartOffer(onOffer: (SessionDescription) -> Unit) {
        reapplyAudioRoute()
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("IceRestart", "true"))
        }
        createDescription("createIceRestartOffer", constraints, offer = true, onOffer)
    }

    fun createAnswer(onAnswer: (SessionDescription) -> Unit) =
        createDescription("createAnswer", MediaConstraints(), offer = false, onAnswer)

    fun setRemoteDescription(type: String, sdp: String, onComplete: () -> Unit = {}) = post("setRemoteDescription") {
        val connection = peerConnection ?: return@post fail("PeerConnection chua duoc tao")
        val connectionGeneration = generation
        val description = SessionDescription(SessionDescription.Type.fromCanonicalForm(type), sdp)
        connection.setRemoteDescription(sdpObserver("setRemoteDescription", connectionGeneration, onSet = {
            post("remote description set") remoteSet@{
                if (!isCurrentGeneration(connectionGeneration)) return@remoteSet
                remoteDescriptionSet = true
                flushPendingRemoteCandidates()
                deliver { onComplete() }
            }
        }), description)
    }

    fun addIceCandidate(sdpMid: String?, sdpMLineIndex: Int, candidate: String) = post("addIceCandidate") {
        val iceCandidate = IceCandidate(sdpMid, sdpMLineIndex, candidate)
        val connection = peerConnection
        if (connection == null || !remoteDescriptionSet) {
            pendingRemoteCandidates += iceCandidate
            return@post
        }
        if (!connection.addIceCandidate(iceCandidate)) fail("Khong them duoc ICE candidate")
    }

    fun setMuted(value: Boolean) {
        muted = value
        applyMediaEnabledState()
    }

    fun toggleMute(): Boolean {
        setMuted(!muted)
        return muted
    }

    fun setCameraEnabled(value: Boolean) {
        cameraEnabled = value
        applyMediaEnabledState()
    }

    fun toggleCamera(): Boolean {
        setCameraEnabled(!cameraEnabled)
        return cameraEnabled
    }

    fun toggleAudioRoute(): CallAudioRoute {
        val next = synchronized(audioLock) { nextAudioRoute(audioRoute) }
        setAudioRoute(next)
        return next
    }

    fun setAudioRoute(route: CallAudioRoute) {
        synchronized(audioLock) { audioRoute = route }
        reapplyAudioRoute()
        deliver { onAudioRouteChanged?.invoke(route) }
    }

    fun setPhoneCallActive(active: Boolean) {
        synchronized(audioLock) { phoneCallActive = active }
        updateInterruptionState(if (active) "Cuoc goi SIM dang chen" else null)
    }

    fun setAudioFocusInterrupted(interrupted: Boolean) {
        synchronized(audioLock) { audioFocusInterrupted = interrupted }
        updateInterruptionState(if (interrupted) "Ung dung khac dang dung am thanh" else null)
    }

    fun switchCamera(onResult: (Boolean) -> Unit = {}) = post("switchCamera") {
        val current = media
        val capturer = current?.capturer
        val nextCamera = current?.let { nextCameraName(it.currentCameraName, it.frontCameraName, it.backCameraName) }
        if (capturer == null || nextCamera == null) {
            fail("Thiet bi chi co mot camera")
            deliver { onResult(false) }
            return@post
        }
        val connectionGeneration = generation
        capturer.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFrontCamera: Boolean) = guardCallback("switchCamera", connectionGeneration) {
                post("camera switched") {
                    if (media === current) {
                        current.currentCameraName = if (isFrontCamera) current.frontCameraName else current.backCameraName
                    }
                    deliver { onCameraFacingChanged?.invoke(isFrontCamera) }
                    deliver { onResult(true) }
                }
            }

            override fun onCameraSwitchError(errorDescription: String?) = guardCallback("switchCamera", connectionGeneration) {
                fail(errorDescription ?: "Khong doi duoc camera")
                deliver { onResult(false) }
            }
        })
    }

    /** Ends the current call's connection; the manager can prepare a new one afterwards. */
    fun reset() {
        endAudioSession()
        // The UI shows the earpiece when a call ends; without this the next call would start on
        // the speaker if it was turned on during this one.
        synchronized(audioLock) { audioRoute = CallAudioRoute.EARPIECE }
        muted = false
        cameraEnabled = true
        post("reset") {
            closeConnection()
            pendingRemoteCandidates.clear()
        }
    }

    /** Frees everything; the manager cannot be used again. */
    fun release() {
        if (disposed) return
        disposed = true
        endAudioSession()
        try {
            webRtcThread.execute {
                try {
                    closeConnection()
                    factory.dispose()
                    audioDeviceModule.release()
                    eglBase.release()
                } catch (error: Throwable) {
                    Log.e(TAG, "WebRTC release failed", error)
                }
            }
        } catch (_: RejectedExecutionException) {
            // Already shut down.
        }
        webRtcThread.shutdown()
    }

    // ---- webRtcThread only -------------------------------------------------------------------

    private fun openConnection(video: Boolean, iceServers: List<PeerConnection.IceServer>) {
        check(!disposed) { "WebRTC da dong" }
        closeConnection()
        val connectionGeneration = generation
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }
        val connection = factory.createPeerConnection(rtcConfig, CallPeerObserver(connectionGeneration, peerEvents))
            ?: error("Khong tao duoc PeerConnection")
        peerConnection = connection
        val created = try {
            createLocalMedia(video)
        } catch (error: Throwable) {
            closeConnection()
            throw error
        }
        media = created
        localVideoTrack = created.videoTrack
        connection.addTrack(created.audioTrack, STREAM_IDS)
        created.videoTrack?.let { connection.addTrack(it, STREAM_IDS) }
        applyMediaEnabledStateNow()
        created.currentCameraName?.let { name -> deliver { onCameraFacingChanged?.invoke(name == created.frontCameraName) } }
        if (created.videoTrack != null) deliver { onLocalVideoReady?.invoke() }
        startQualityMonitoring()
    }

    private fun closeConnection() {
        generation += 1
        statsTask?.cancel(false)
        statsTask = null
        lastCallQuality = null
        val connection = peerConnection
        val oldMedia = media
        peerConnection = null
        media = null
        localVideoTrack = null
        remoteVideoTrack = null
        remoteDescriptionSet = false
        connection?.close()
        connection?.dispose()
        oldMedia?.dispose()
    }

    private fun createLocalMedia(video: Boolean): LocalMedia {
        val audioSource = factory.createAudioSource(MediaConstraints())
        val audioTrack = factory.createAudioTrack("social-audio", audioSource)
        if (!video) return LocalMedia(audioSource, audioTrack)

        try {
            val enumerator = Camera2Enumerator(appContext)
            val frontCameraName = enumerator.deviceNames.firstOrNull { enumerator.isFrontFacing(it) }
            val backCameraName = enumerator.deviceNames.firstOrNull { enumerator.isBackFacing(it) }
            val cameraName = frontCameraName ?: backCameraName ?: error("Khong tim thay camera")
            val capturer = enumerator.createCapturer(cameraName, cameraEvents)
                ?: error("Khong tao duoc camera capturer")
            val textureHelper = SurfaceTextureHelper.create("social-camera", eglBase.eglBaseContext)
            val videoSource = factory.createVideoSource(capturer.isScreencast)
            capturer.initialize(textureHelper, appContext, videoSource.capturerObserver)
            capturer.startCapture(1280, 720, 30)
            val videoTrack = factory.createVideoTrack("social-video", videoSource)
            return LocalMedia(
                audioSource, audioTrack, videoSource, videoTrack, capturer, textureHelper,
                frontCameraName, backCameraName, cameraName
            )
        } catch (error: Throwable) {
            audioTrack.dispose()
            audioSource.dispose()
            throw error
        }
    }

    private fun createDescription(
        what: String,
        constraints: MediaConstraints,
        offer: Boolean,
        onCreated: (SessionDescription) -> Unit
    ) = post(what) {
        val connection = peerConnection ?: return@post fail("PeerConnection chua duoc tao")
        val connectionGeneration = generation
        val observer = sdpObserver(what, connectionGeneration, onCreate = { description ->
            // Delivered on the signaling thread: hop back instead of calling the PeerConnection here.
            post("setLocalDescription") setLocal@{
                if (!isCurrentGeneration(connectionGeneration)) return@setLocal
                val current = peerConnection ?: return@setLocal
                current.setLocalDescription(sdpObserver("setLocalDescription", connectionGeneration, onSet = {
                    post("local description set") {
                        if (isCurrentGeneration(connectionGeneration)) deliver { onCreated(description) }
                    }
                }), description)
            }
        })
        if (offer) connection.createOffer(observer, constraints) else connection.createAnswer(observer, constraints)
    }

    private fun flushPendingRemoteCandidates() {
        val connection = peerConnection ?: return
        val queued = pendingRemoteCandidates.toList()
        pendingRemoteCandidates.clear()
        queued.forEach { candidate ->
            if (!connection.addIceCandidate(candidate)) fail("Khong them duoc ICE candidate")
        }
    }

    private fun applyMediaEnabledStateNow() {
        val current = media ?: return
        val interrupted = mediaInterrupted
        current.audioTrack.setEnabled(!muted && !interrupted)
        current.videoTrack?.setEnabled(cameraEnabled && !interrupted)
    }

    private fun startQualityMonitoring() {
        statsTask?.cancel(false)
        lastCallQuality = null
        val connectionGeneration = generation
        statsTask = webRtcThread.scheduleWithFixedDelay({
            try {
                collectCallQuality(connectionGeneration)
            } catch (error: Throwable) {
                reportError("getStats", error)
            }
        }, 0L, QUALITY_SAMPLE_INTERVAL_MS, TimeUnit.MILLISECONDS)
    }

    private fun collectCallQuality(connectionGeneration: Long) {
        if (!isCurrentGeneration(connectionGeneration)) return
        val connection = peerConnection ?: return
        connection.getStats(RTCStatsCollectorCallback { report ->
            guardCallback("getStats", connectionGeneration) {
                val quality = parseCallQuality(report.statsMap.values) ?: return@guardCallback
                post("call quality") {
                    if (quality != lastCallQuality) {
                        lastCallQuality = quality
                        deliver { onCallQualityChanged?.invoke(quality) }
                    }
                }
            }
        })
    }

    // ---- callbacks from WebRTC threads -------------------------------------------------------

    private val peerEvents = object : CallPeerObserver.Events {
        override fun isCurrentGeneration(generation: Long): Boolean =
            this@WebRtcCallManager.isCurrentGeneration(generation)

        override fun onLocalIceCandidate(generation: Long, candidate: IceCandidate) {
            onIceCandidateReady?.invoke(candidate)
        }

        override fun onConnectionState(generation: Long, state: String) {
            onConnectionStateChanged?.invoke(state)
        }

        override fun onRemoteVideoTrack(generation: Long, track: VideoTrack) {
            remoteVideoTrack = track
            onRemoteVideoReady?.invoke()
        }

        override fun onCallbackError(callback: String, error: Throwable) {
            reportError("WebRTC callback $callback", error)
        }
    }

    private val cameraEvents = object : CameraVideoCapturer.CameraEventsHandler {
        override fun onCameraError(error: String?) = fail(error ?: "Loi camera")
        override fun onCameraDisconnected() = fail("Camera da ngat ket noi")
        override fun onCameraFreezed(error: String?) = fail(error ?: "Camera bi dung")
        override fun onCameraOpening(cameraName: String?) = Unit
        override fun onFirstFrameAvailable() = Unit
        override fun onCameraClosed() = Unit
    }

    private fun sdpObserver(
        what: String,
        connectionGeneration: Long,
        onCreate: (SessionDescription) -> Unit = {},
        onSet: () -> Unit = {}
    ) = object : SdpObserver {
        override fun onCreateSuccess(description: SessionDescription?) = guardCallback(what, connectionGeneration) {
            if (description == null) fail("SDP rong") else onCreate(description)
        }

        override fun onSetSuccess() = guardCallback(what, connectionGeneration) { onSet() }

        override fun onCreateFailure(error: String?) = guardCallback(what, connectionGeneration) {
            fail(error ?: "Khong tao duoc SDP")
        }

        override fun onSetFailure(error: String?) = guardCallback(what, connectionGeneration) {
            fail(error ?: "Khong dat duoc SDP")
        }
    }

    // ---- helpers -----------------------------------------------------------------------------

    private fun isCurrentGeneration(connectionGeneration: Long): Boolean =
        shouldAcceptWebRtcCallback(
            disposed = disposed,
            sameConnection = true,
            callbackGeneration = connectionGeneration,
            currentGeneration = generation
        )

    /** Queues [block] on [webRtcThread]; a failure is reported instead of thrown. */
    private fun post(what: String, block: () -> Unit) {
        if (disposed) return
        try {
            webRtcThread.execute {
                try {
                    block()
                } catch (error: Throwable) {
                    reportError(what, error)
                }
            }
        } catch (_: RejectedExecutionException) {
            // Released while the call was ending.
        }
    }

    private suspend fun <T> onWebRtcThread(block: () -> T): T = suspendCancellableCoroutine { continuation ->
        try {
            webRtcThread.execute { continuation.resumeWith(runCatching(block)) }
        } catch (rejected: RejectedExecutionException) {
            continuation.resumeWithException(IllegalStateException("WebRTC da dong", rejected))
        }
    }

    /** Runs a WebRTC callback body; nothing may escape into WebRTC's native frame. */
    private inline fun guardCallback(what: String, connectionGeneration: Long, block: () -> Unit) {
        try {
            if (isCurrentGeneration(connectionGeneration)) block()
        } catch (error: Throwable) {
            reportError(what, error)
        }
    }

    /** Invokes an app callback so that a bug there cannot stop the WebRTC thread. */
    private inline fun deliver(block: () -> Unit) {
        try {
            block()
        } catch (error: Throwable) {
            reportError("app callback", error)
        }
    }

    private fun reportError(what: String, error: Throwable) {
        try {
            Log.e(TAG, "$what failed", error)
        } catch (_: Throwable) {
            // Logging must never be the reason a WebRTC thread dies.
        }
        fail("$what: ${error.message ?: error.javaClass.simpleName}")
    }

    private fun fail(message: String) {
        try {
            onFailure?.invoke(message)
        } catch (_: Throwable) {
            // The app's handler failed too; there is nobody left to tell.
        }
    }

    // ---- audio -------------------------------------------------------------------------------

    private fun applyMediaEnabledState() = post("applyMediaEnabledState") { applyMediaEnabledStateNow() }

    private fun beginAudioSession() {
        synchronized(audioLock) {
            if (!audioSessionActive) {
                previousAudioMode = audioManager.mode
                previousSpeakerphoneOn = audioManager.isSpeakerphoneOn
                audioSessionActive = true
                audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                requestAudioFocus()
                registerPhoneStateListener()
            }
        }
        reapplyAudioRoute()
    }

    private fun reapplyAudioRoute() {
        synchronized(audioLock) {
            if (!audioSessionActive) return
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            @Suppress("DEPRECATION")
            audioManager.isSpeakerphoneOn = audioRoute == CallAudioRoute.SPEAKER
        }
    }

    private fun endAudioSession() {
        synchronized(audioLock) {
            if (!audioSessionActive) return
            unregisterPhoneStateListener()
            abandonAudioFocus()
            @Suppress("DEPRECATION")
            audioManager.isSpeakerphoneOn = previousSpeakerphoneOn
            audioManager.mode = previousAudioMode
            audioSessionActive = false
            audioFocusInterrupted = false
            phoneCallActive = false
            mediaInterrupted = false
        }
    }

    private fun updateInterruptionState(reason: String?) {
        val interrupted = synchronized(audioLock) {
            val now = phoneCallActive || audioFocusInterrupted
            if (mediaInterrupted == now) return
            mediaInterrupted = now
            now
        }
        applyMediaEnabledState()
        deliver { onCallInterruptionChanged?.invoke(interrupted, if (interrupted) reason else null) }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setOnAudioFocusChangeListener(audioFocusListener)
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusListener,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let(audioManager::abandonAudioFocusRequest)
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusListener)
        }
    }

    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_GAIN -> setAudioFocusInterrupted(false)
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> setAudioFocusInterrupted(true)
        }
    }

    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            setPhoneCallActive(state != TelephonyManager.CALL_STATE_IDLE)
        }
    }

    private fun registerPhoneStateListener() {
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) return
        @Suppress("DEPRECATION")
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun unregisterPhoneStateListener() {
        @Suppress("DEPRECATION")
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
    }

    // ---- parsing -----------------------------------------------------------------------------

    private fun parseCallQuality(stats: Collection<RTCStats>): CallQuality? {
        var rttMs: Int? = null
        var jitterMs: Int? = null
        var packetsLost = 0.0
        var packetsReceived = 0.0
        var hasPacketCounters = false

        stats.forEach { stat ->
            val members = stat.members
            when (stat.type) {
                "candidate-pair" -> {
                    val state = members["state"]?.toString()
                    val roundTripSeconds = members["currentRoundTripTime"].asDouble()
                    if (state == "succeeded" && roundTripSeconds != null) {
                        val value = (roundTripSeconds * 1000.0).toInt()
                        rttMs = if (rttMs == null) value else minOf(rttMs!!, value)
                    }
                }
                "inbound-rtp" -> {
                    val received = members["packetsReceived"].asDouble()
                    val lost = members["packetsLost"].asDouble()
                    if (received != null || lost != null) {
                        hasPacketCounters = true
                        packetsReceived += received ?: 0.0
                        packetsLost += lost ?: 0.0
                    }
                    val jitterSeconds = members["jitter"].asDouble()
                    if (jitterSeconds != null) {
                        val value = (jitterSeconds * 1000.0).toInt()
                        jitterMs = if (jitterMs == null) value else maxOf(jitterMs!!, value)
                    }
                }
            }
        }

        val packetLossPercent = if (hasPacketCounters && packetsReceived + packetsLost > 0) {
            ((packetsLost / (packetsReceived + packetsLost)) * 100.0).toInt()
        } else {
            null
        }
        if (rttMs == null && packetLossPercent == null && jitterMs == null) return null
        return CallQuality(
            level = classifyCallQuality(rttMs, packetLossPercent, jitterMs),
            rttMs = rttMs,
            packetLossPercent = packetLossPercent,
            jitterMs = jitterMs
        )
    }

    private fun Any?.asDouble(): Double? = when (this) {
        is Number -> toDouble()
        is String -> toDoubleOrNull()
        else -> null
    }

    private fun parseIceServers(json: JSONArray): List<PeerConnection.IceServer> = buildList {
        for (index in 0 until json.length()) {
            val item = json.optJSONObject(index) ?: continue
            val urls = item.optJSONArray("urls")?.let { array ->
                (0 until array.length()).mapNotNull { array.optString(it).takeIf(String::isNotBlank) }
            } ?: item.optStringOrNull("urls")?.let(::listOf)
            if (urls.isNullOrEmpty()) continue
            val builder = PeerConnection.IceServer.builder(urls)
            item.optStringOrNull("username")?.let(builder::setUsername)
            item.optStringOrNull("credential")?.let(builder::setPassword)
            add(builder.createIceServer())
        }
    }

    companion object {
        private const val TAG = "SocialCall"
        private const val QUALITY_SAMPLE_INTERVAL_MS = 2_000L
        private val STREAM_IDS = listOf("social-call")
        private val initialized = AtomicBoolean(false)

        private fun initializeWebRtc(context: Context) {
            if (initialized.compareAndSet(false, true)) {
                PeerConnectionFactory.initialize(
                    PeerConnectionFactory.InitializationOptions.builder(context)
                        .setEnableInternalTracer(false)
                        .createInitializationOptions()
                )
            }
        }
    }
}

internal fun shouldAcceptWebRtcCallback(
    disposed: Boolean,
    sameConnection: Boolean,
    callbackGeneration: Long,
    currentGeneration: Long
): Boolean = !disposed && sameConnection && callbackGeneration == currentGeneration

internal fun nextCameraName(current: String?, front: String?, back: String?): String? = when {
    current == front -> back
    current == back -> front
    else -> front ?: back
}
