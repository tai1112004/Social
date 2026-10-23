package com.taihoang.socialmobile

import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.VideoTrack

/**
 * Callbacks of one PeerConnection. WebRTC calls them on its signaling thread and checks for a
 * pending Java exception after each one, aborting the whole process if it finds one. So every
 * callback is guarded, and none of them waits for a lock or calls back into the PeerConnection:
 * they only hand the event to [Events].
 *
 * [generation] identifies the connection this observer belongs to, so late callbacks from a
 * connection that has already been replaced are dropped.
 */
internal class CallPeerObserver(
    private val generation: Long,
    private val events: Events
) : PeerConnection.Observer {

    interface Events {
        fun isCurrentGeneration(generation: Long): Boolean
        fun onLocalIceCandidate(generation: Long, candidate: IceCandidate)
        fun onConnectionState(generation: Long, state: String)
        fun onRemoteVideoTrack(generation: Long, track: VideoTrack)
        fun onCallbackError(callback: String, error: Throwable)
    }

    override fun onIceCandidate(candidate: IceCandidate?) = guard("onIceCandidate") {
        if (candidate != null) events.onLocalIceCandidate(generation, candidate)
    }

    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) = guard("onIceConnectionChange") {
        events.onConnectionState(generation, state?.name ?: "UNKNOWN")
    }

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) = guard("onConnectionChange") {
        events.onConnectionState(generation, newState?.name ?: "UNKNOWN")
    }

    // Unified Plan reports every remote track here; onAddTrack/onAddStream are legacy duplicates.
    // The receiver is only valid during the callback, so the track is read now.
    override fun onTrack(transceiver: RtpTransceiver?) = guard("onTrack") {
        val track = transceiver?.receiver?.track()
        if (track is VideoTrack) events.onRemoteVideoTrack(generation, track)
    }

    override fun onSignalingChange(state: PeerConnection.SignalingState?) = Unit
    override fun onIceConnectionReceivingChange(receiving: Boolean) = Unit
    override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) = Unit
    override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) = Unit
    override fun onAddStream(stream: MediaStream?) = Unit
    override fun onRemoveStream(stream: MediaStream?) = Unit
    override fun onDataChannel(dataChannel: DataChannel?) = Unit
    override fun onRenegotiationNeeded() = Unit
    override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) = Unit

    private inline fun guard(callback: String, block: () -> Unit) {
        try {
            if (events.isCurrentGeneration(generation)) block()
        } catch (error: Throwable) {
            try {
                events.onCallbackError(callback, error)
            } catch (_: Throwable) {
                // Nothing may escape into WebRTC's native frame.
            }
        }
    }
}
