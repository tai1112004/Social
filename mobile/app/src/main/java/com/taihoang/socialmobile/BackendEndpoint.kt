package com.taihoang.socialmobile

data class BackendEndpoint(
    val id: String,
    val label: String,
    val baseUrl: String
)

val LOCAL_BACKEND_ENDPOINT = BackendEndpoint(
    id = "local",
    label = "Local 8080",
    baseUrl = "http://127.0.0.1:8080"
)

val DEV_TUNNEL_BACKEND_ENDPOINT = BackendEndpoint(
    id = "dev_tunnel",
    label = "VS Code Dev Tunnel",
    baseUrl = "https://z2sb51jm-8080.asse.devtunnels.ms"
)

val BACKEND_ENDPOINTS = listOf(LOCAL_BACKEND_ENDPOINT, DEV_TUNNEL_BACKEND_ENDPOINT)

fun normalizeBackendUrl(baseUrl: String): String = baseUrl.trim().trimEnd('/')

fun backendEndpointForId(id: String?): BackendEndpoint? =
    BACKEND_ENDPOINTS.firstOrNull { it.id == id }

fun backendEndpointIdForUrl(baseUrl: String): String =
    BACKEND_ENDPOINTS.firstOrNull { normalizeBackendUrl(it.baseUrl).equals(normalizeBackendUrl(baseUrl), ignoreCase = true) }?.id
        ?: LOCAL_BACKEND_ENDPOINT.id

fun websocketUrlForBackend(baseUrl: String): String {
    val normalized = normalizeBackendUrl(baseUrl)
    val authority = normalized.substringAfter("://", normalized)
    val scheme = when {
        normalized.startsWith("https://", ignoreCase = true) -> "wss"
        normalized.startsWith("http://", ignoreCase = true) -> "ws"
        normalized.contains("://") -> normalized.substringBefore("://")
        else -> "ws"
    }
    return "$scheme://$authority/ws"
}

fun stompHostForBackend(baseUrl: String): String =
    normalizeBackendUrl(baseUrl).substringAfter("://", normalizeBackendUrl(baseUrl)).substringBefore('/')
