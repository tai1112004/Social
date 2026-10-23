package com.taihoang.socialmobile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallReconnectPolicyTest {
    @Test
    fun onlyDisconnectedAndFailedStatesTriggerRecovery() {
        assertTrue(isRecoverableCallState("DISCONNECTED"))
        assertTrue(isRecoverableCallState("failed"))
        assertFalse(isRecoverableCallState("CONNECTED"))
        assertFalse(isRecoverableCallState("CLOSED"))
    }

    @Test
    fun retryBackoffIsBoundedToThreeAttempts() {
        assertEquals(1_000L, callReconnectDelayMs(0))
        assertEquals(2_000L, callReconnectDelayMs(1))
        assertEquals(4_000L, callReconnectDelayMs(2))
        assertEquals(4_000L, callReconnectDelayMs(9))
    }
}
