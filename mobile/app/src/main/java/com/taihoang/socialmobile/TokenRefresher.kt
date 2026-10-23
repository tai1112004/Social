package com.taihoang.socialmobile

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Storage seam so [TokenRefresher] can be tested without SharedPreferences. */
interface SessionHolder {
    fun get(): AuthSession?
    fun save(session: AuthSession)
    fun clear()
}

/**
 * Renews the access token after the backend rejects it.
 * The backend rotates the refresh token on every call, so concurrent callers must share one refresh:
 * a second POST /auth/refresh with the already-rotated token would fail and log the user out.
 */
internal class TokenRefresher(
    private val sessions: SessionHolder,
    private val refreshCall: suspend (refreshToken: String) -> AuthSession
) {
    private val mutex = Mutex()

    @Volatile var onRefreshed: ((AuthSession) -> Unit)? = null
    @Volatile var onExpired: (() -> Unit)? = null

    /**
     * Returns a session whose token differs from [staleToken], or null when the session cannot be
     * renewed. A rejected refresh clears the stored session and fires [onExpired]; network errors
     * propagate and keep the session so the caller can retry later.
     */
    suspend fun refreshAfterRejected(staleToken: String): AuthSession? = mutex.withLock {
        val current = sessions.get() ?: return@withLock null
        // Another caller already refreshed while this one waited for the lock.
        if (current.token != staleToken) return@withLock current

        val refreshToken = current.refreshToken
        if (refreshToken.isNullOrBlank()) {
            expire()
            return@withLock null
        }

        val refreshed = try {
            refreshCall(refreshToken)
        } catch (exception: ApiException) {
            if (exception.status in REJECTED_STATUSES) {
                expire()
                return@withLock null
            }
            throw exception
        }

        // The user logged out (or signed in again) while the refresh was in flight.
        if (sessions.get()?.token != staleToken) return@withLock null
        if (refreshed.token.isBlank()) {
            expire()
            return@withLock null
        }

        sessions.save(refreshed)
        onRefreshed?.invoke(refreshed)
        refreshed
    }

    private fun expire() {
        sessions.clear()
        onExpired?.invoke()
    }

    private companion object {
        val REJECTED_STATUSES = setOf(400, 401, 403)
    }
}
