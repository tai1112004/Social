package com.taihoang.socialmobile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallLifecyclePolicyTest {
    @Test
    fun keepsForegroundServiceForIncomingAndActiveCalls() {
        assertTrue(shouldRunCallForegroundService("INCOMING"))
        assertTrue(shouldRunCallForegroundService("ACTIVE"))
        assertTrue(shouldRunCallForegroundService("RECONNECTING"))
    }

    @Test
    fun stopsForegroundServiceWhenCallIsIdle() {
        assertFalse(shouldRunCallForegroundService("IDLE"))
    }

    @Test
    fun ignoresCallbacksFromAClosedOrReplacedPeerConnection() {
        assertFalse(shouldAcceptWebRtcCallback(false, true, 1L, 2L))
        assertFalse(shouldAcceptWebRtcCallback(true, true, 2L, 2L))
        assertFalse(shouldAcceptWebRtcCallback(false, false, 2L, 2L))
        assertTrue(shouldAcceptWebRtcCallback(false, true, 2L, 2L))
    }
}
