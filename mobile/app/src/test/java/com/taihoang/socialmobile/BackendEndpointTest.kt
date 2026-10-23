package com.taihoang.socialmobile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BackendEndpointTest {
    @Test
    fun endpointUrlsAreStable() {
        assertEquals("http://127.0.0.1:8080", LOCAL_BACKEND_ENDPOINT.baseUrl)
        assertEquals("https://z2sb51jm-8080.asse.devtunnels.ms", DEV_TUNNEL_BACKEND_ENDPOINT.baseUrl)
    }

    @Test
    fun trailingSlashesAreRemoved() {
        assertEquals("https://z2sb51jm-8080.asse.devtunnels.ms", normalizeBackendUrl(" https://z2sb51jm-8080.asse.devtunnels.ms/// "))
    }

    @Test
    fun endpointIdMatchesConfiguredUrl() {
        assertEquals("local", backendEndpointIdForUrl("http://127.0.0.1:8080/"))
        assertEquals("dev_tunnel", backendEndpointIdForUrl("HTTPS://Z2SB51JM-8080.ASSE.DEVTUNNELS.MS/"))
    }

    @Test
    fun backendUrlsMapToWebSocketUrls() {
        assertEquals("ws://127.0.0.1:8080/ws", websocketUrlForBackend(LOCAL_BACKEND_ENDPOINT.baseUrl))
        assertEquals("wss://z2sb51jm-8080.asse.devtunnels.ms/ws", websocketUrlForBackend(DEV_TUNNEL_BACKEND_ENDPOINT.baseUrl + "/"))
        assertEquals("127.0.0.1:8080", stompHostForBackend(LOCAL_BACKEND_ENDPOINT.baseUrl))
        assertEquals("z2sb51jm-8080.asse.devtunnels.ms", stompHostForBackend(DEV_TUNNEL_BACKEND_ENDPOINT.baseUrl))
    }

    @Test
    fun unknownEndpointFallsBackToLocal() {
        assertEquals("local", backendEndpointIdForUrl("http://unknown.example:8080"))
        assertNull(backendEndpointForId("unknown"))
    }
}
