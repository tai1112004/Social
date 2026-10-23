package com.taihoang.socialmobile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.VideoTrack

class CallPeerObserverTest {
    private val hostCandidate = IceCandidate("0", 0, "candidate:1 1 udp 2122260223 192.168.1.110 39117 typ host")

    @Test
    fun localCandidateIsForwardedExactlyOnce() {
        // The old observer called its own onIceCandidate again, overflowing the signaling
        // thread's stack and aborting the app at the first local candidate.
        val events = RecordingEvents()

        CallPeerObserver(generation = 1L, events = events).onIceCandidate(hostCandidate)

        assertEquals(listOf(hostCandidate), events.candidates)
    }

    @Test
    fun exceptionInTheAppNeverReachesWebRtc() {
        val events = RecordingEvents(failOnCandidate = true)

        CallPeerObserver(generation = 1L, events = events).onIceCandidate(hostCandidate)

        assertEquals(listOf("onIceCandidate"), events.errors)
    }

    @Test
    fun callbacksOfAReplacedConnectionAreDropped() {
        val events = RecordingEvents(currentGeneration = 2L)
        val staleObserver = CallPeerObserver(generation = 1L, events = events)

        staleObserver.onIceCandidate(hostCandidate)
        staleObserver.onIceConnectionChange(PeerConnection.IceConnectionState.CONNECTED)

        assertTrue(events.candidates.isEmpty())
        assertTrue(events.states.isEmpty())
    }

    @Test
    fun bothConnectionStateCallbacksAreForwarded() {
        val events = RecordingEvents()
        val observer = CallPeerObserver(generation = 1L, events = events)

        observer.onIceConnectionChange(PeerConnection.IceConnectionState.CHECKING)
        observer.onConnectionChange(PeerConnection.PeerConnectionState.CONNECTED)

        assertEquals(listOf("CHECKING", "CONNECTED"), events.states)
    }

    private class RecordingEvents(
        private val currentGeneration: Long = 1L,
        private val failOnCandidate: Boolean = false
    ) : CallPeerObserver.Events {
        val candidates = mutableListOf<IceCandidate>()
        val states = mutableListOf<String>()
        val errors = mutableListOf<String>()

        override fun isCurrentGeneration(generation: Long) = generation == currentGeneration

        override fun onLocalIceCandidate(generation: Long, candidate: IceCandidate) {
            if (failOnCandidate) throw IllegalStateException("app bug")
            candidates += candidate
        }

        override fun onConnectionState(generation: Long, state: String) {
            states += state
        }

        override fun onRemoteVideoTrack(generation: Long, track: VideoTrack) = Unit

        override fun onCallbackError(callback: String, error: Throwable) {
            errors += callback
        }
    }
}
