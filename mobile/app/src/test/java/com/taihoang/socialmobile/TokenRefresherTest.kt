package com.taihoang.socialmobile

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class TokenRefresherTest {
    @Test
    fun concurrentRejectionsShareOneRefresh() = runBlocking {
        val store = FakeSessionHolder(session("old", "refresh-1"))
        val calls = AtomicInteger()
        val release = CompletableDeferred<Unit>()
        val refresher = TokenRefresher(store) { refreshToken ->
            calls.incrementAndGet()
            release.await()
            session("new", "$refreshToken-rotated")
        }
        var refreshedCallbacks = 0
        refresher.onRefreshed = { refreshedCallbacks += 1 }

        val results = (1..3).map { async(Dispatchers.Default) { refresher.refreshAfterRejected("old") } }
        release.complete(Unit)

        assertEquals(listOf("new", "new", "new"), results.awaitAll().map { it?.token })
        assertEquals(1, calls.get())
        assertEquals(1, refreshedCallbacks)
        assertEquals("refresh-1-rotated", store.get()?.refreshToken)
    }

    @Test
    fun alreadyRefreshedTokenIsReturnedWithoutCallingBackend() = runBlocking {
        val store = FakeSessionHolder(session("new", "refresh-2"))
        val refresher = TokenRefresher(store) { fail("refresh must not be called"); throw IllegalStateException() }

        assertEquals("new", refresher.refreshAfterRejected("old")?.token)
    }

    @Test
    fun rejectedRefreshTokenClearsSessionAndReportsExpiry() = runBlocking {
        val store = FakeSessionHolder(session("old", "revoked"))
        val refresher = TokenRefresher(store) { throw ApiException(401, "Session da bi thu hoi") }
        var expired = false
        refresher.onExpired = { expired = true }

        assertNull(refresher.refreshAfterRejected("old"))
        assertNull(store.get())
        assertTrue(expired)
    }

    @Test
    fun missingRefreshTokenExpiresSession() = runBlocking {
        val store = FakeSessionHolder(session("old", null))
        var refreshCalled = false
        val refresher = TokenRefresher(store) { refreshCalled = true; session("new", "x") }

        assertNull(refresher.refreshAfterRejected("old"))
        assertNull(store.get())
        assertTrue(!refreshCalled)
    }

    @Test
    fun networkFailureKeepsSessionForLaterRetry() = runBlocking {
        val store = FakeSessionHolder(session("old", "refresh-1"))
        val refresher = TokenRefresher(store) { throw IOException("offline") }

        try {
            refresher.refreshAfterRejected("old")
            fail("network errors must propagate")
        } catch (expected: IOException) {
            assertEquals("old", store.get()?.token)
        }
    }

    @Test
    fun logoutDuringRefreshDoesNotRestoreSession() = runBlocking {
        val store = FakeSessionHolder(session("old", "refresh-1"))
        val refresher = TokenRefresher(store) {
            store.clear()
            session("new", "refresh-2")
        }

        assertNull(refresher.refreshAfterRejected("old"))
        assertNull(store.get())
    }

    private fun session(token: String, refreshToken: String?) =
        AuthSession(token = token, refreshToken = refreshToken, userId = 7L, userName = "Tai", email = "tai@example.test")

    private class FakeSessionHolder(initial: AuthSession?) : SessionHolder {
        @Volatile private var current: AuthSession? = initial
        override fun get(): AuthSession? = current
        override fun save(session: AuthSession) { current = session }
        override fun clear() { current = null }
    }
}
