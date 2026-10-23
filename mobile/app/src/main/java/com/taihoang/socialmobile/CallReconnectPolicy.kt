package com.taihoang.socialmobile

internal const val CALL_RECONNECT_MAX_ATTEMPTS = 3

internal fun isRecoverableCallState(state: String): Boolean =
    state.equals("DISCONNECTED", ignoreCase = true) || state.equals("FAILED", ignoreCase = true)

internal fun callReconnectDelayMs(attempt: Int): Long =
    1_000L * (1L shl attempt.coerceIn(0, CALL_RECONNECT_MAX_ATTEMPTS - 1))
