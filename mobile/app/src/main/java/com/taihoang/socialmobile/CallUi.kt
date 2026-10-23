package com.taihoang.socialmobile

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

private val CallBackground = Color(0xFFF0F2F5)
private val CallInk = Color(0xFF111827)
private val CallMuted = Color(0xFF65676B)
private val CallBrand = Color(0xFF1877F2)

@Composable
@Suppress("UNUSED_PARAMETER")
fun CallOverlay(
    call: CallEventModel,
    phase: String,
    manager: WebRtcCallManager,
    mediaVersion: Long = 0L,
    errorMessage: String? = null,
    muted: Boolean,
    cameraEnabled: Boolean,
    frontCamera: Boolean,
    audioRoute: CallAudioRoute,
    interruptionReason: String?,
    quality: CallQuality?,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit,
    onEnd: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleCamera: () -> Unit,
    onSwitchCamera: () -> Unit,
    onToggleAudioRoute: () -> Unit
) {
    val isVideo = call.callType.equals("VIDEO", true)
    val title = if (call.callerId == call.calleeId) "Cuoc goi" else if (phase == "INCOMING") call.callerName else call.calleeName
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF101827)) {
        Box(Modifier.fillMaxSize()) {
            if (isVideo && phase != "INCOMING") {
                CallVideoSurface(manager.remoteVideo, manager.eglContext, mirror = false, Modifier.fillMaxSize())
                CallVideoSurface(
                    manager.localVideo,
                    manager.eglContext,
                    mirror = frontCamera,
                    Modifier
                        .padding(top = 26.dp, end = 16.dp)
                        .size(width = 112.dp, height = 160.dp)
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            Column(
                Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(if (isVideo) "Goi video 1-1" else "Goi audio 1-1", color = Color.White.copy(alpha = .7f), fontSize = 13.sp)
                Spacer(Modifier.height(22.dp))
                Text(title, color = Color.White, fontSize = 25.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    when {
                        phase == "INTERRUPTED" && !interruptionReason.isNullOrBlank() -> interruptionReason
                        phase == "INCOMING" -> "Cuoc goi den"
                        phase == "OUTGOING" -> "Dang goi..."
                        phase == "ACTIVE" -> "Dang ket noi"
                        phase == "RECONNECTING" -> "Dang khoi phuc ket noi"
                        else -> phase
                    },
                    color = Color.White.copy(alpha = .72f),
                    fontSize = 14.sp
                )
                errorMessage?.let { message ->
                    Text(message, color = Color(0xFFFFB4AB), fontSize = 12.sp)
                }
                quality?.let { sample ->
                    Text(
                        "${sample.level.label} | RTT ${sample.rttMs?.let { "${it}ms" } ?: "-"} | Mat goi ${sample.packetLossPercent?.let { "$it%" } ?: "-"} | Jitter ${sample.jitterMs?.let { "${it}ms" } ?: "-"}",
                        color = Color.White.copy(alpha = .62f),
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.weight(1f))
                if (phase == "INCOMING") {
                    Row(horizontalArrangement = Arrangement.spacedBy(42.dp), verticalAlignment = Alignment.CenterVertically) {
                        CallActionButton(Icons.Filled.CallEnd, "Tu choi", Color(0xFFD93025), onReject)
                        CallActionButton(Icons.Filled.Call, "Nhan", Color(0xFF1FA463), onAccept)
                    }
                } else {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CallActionButton(if (muted) Icons.Filled.MicOff else Icons.Filled.Mic, if (muted) "Bat mic" else "Tat mic", Color(0xFF344054), onToggleMute)
                        CallActionButton(
                            Icons.Filled.VolumeUp,
                            if (audioRoute == CallAudioRoute.SPEAKER) "Dung loa thoai" else "Bat loa ngoai",
                            Color(0xFF344054),
                            onToggleAudioRoute
                        )
                        if (isVideo) {
                            CallActionButton(
                                if (cameraEnabled) Icons.Filled.VideocamOff else Icons.Filled.Videocam,
                                if (cameraEnabled) "Tat camera" else "Bat camera",
                                Color(0xFF344054),
                                onToggleCamera
                            )
                            CallActionButton(Icons.Filled.Cameraswitch, "Doi camera", Color(0xFF344054), onSwitchCamera)
                        }
                        CallActionButton(Icons.Filled.CallEnd, "Ket thuc", Color(0xFFD93025), if (phase == "OUTGOING") onCancel else onEnd)
                    }
                }
            }
        }
    }
}

@Composable
private fun CallActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.size(58.dp).clip(CircleShape).background(color)) {
            Icon(icon, label, tint = Color.White)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color.White.copy(alpha = .8f), fontSize = 11.sp)
    }
}

@Composable
private fun CallVideoSurface(track: VideoTrack?, eglContext: EglBase.Context, mirror: Boolean, modifier: Modifier) {
    val renderer = remember { arrayOfNulls<SurfaceViewRenderer>(1) }
    val attachedTrack = remember { arrayOfNulls<VideoTrack>(1) }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            SurfaceViewRenderer(context).also { view ->
                view.init(eglContext, null)
                view.setEnableHardwareScaler(true)
                view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                view.setMirror(mirror)
                renderer[0] = view
            }
        },
        update = { view ->
            if (attachedTrack[0] !== track) {
                attachedTrack[0]?.removeSink(view)
                track?.addSink(view)
                attachedTrack[0] = track
            }
        }
    )
    DisposableEffect(renderer) {
        onDispose {
            renderer[0]?.let { view ->
                attachedTrack[0]?.removeSink(view)
                view.release()
            }
            renderer[0] = null
            attachedTrack[0] = null
        }
    }
}

@Composable
fun CallHistoryScreen(history: List<ApiCallHistory>, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(CallBackground)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.Call, "Quay lai", tint = CallInk) }
            Text("Lich su cuoc goi", color = CallInk, fontSize = 20.sp)
        }
        if (history.isEmpty()) {
            Text("Chua co cuoc goi", color = CallMuted, modifier = Modifier.padding(24.dp))
        } else {
            LazyColumn {
                items(history, key = { it.id }) { item ->
                    val failed = item.status.equals("REJECTED", true) || item.status.equals("MISSED", true) || item.status.equals("FAILED", true)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (item.callType.equals("VIDEO", true)) Icons.Filled.Videocam else Icons.Filled.Call, "Loai cuoc goi", tint = if (failed) Color(0xFFD93025) else CallBrand)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.otherUserName, color = CallInk)
                                Text(
                                    "${if (item.outgoing) "Goi di" else "Goi den"} - ${callStatusLabel(item.status)}",
                                    color = if (failed) Color(0xFFD93025) else CallMuted,
                                    fontSize = 12.sp
                                )
                            }
                            item.durationSeconds?.let { Text("${it}s", color = CallMuted, fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
    }
}

private fun callStatusLabel(status: String): String = when (status.uppercase()) {
    "RINGING" -> "Dang do chuong"
    "ACCEPTED" -> "Da nhan"
    "REJECTED" -> "Tu choi"
    "CANCELLED" -> "Da huy"
    "MISSED" -> "Cuoc goi nho"
    "ENDED" -> "Da ket thuc"
    "FAILED" -> "That bai"
    "BUSY" -> "Dang ban"
    else -> status
}
