package com.taihoang.social_backend.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MeteredTurnService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MeteredTurnService.class);

    private final ObjectMapper objectMapper;

    @Value("${webrtc.metered.api-key:}")
    private String apiKey;

    @Value("${webrtc.metered.credentials-url:}")
    private String credentialsUrl;

    @Value("${webrtc.metered.cache-ttl-seconds:300}")
    private long cacheTtlSeconds = 300L;

    private Clock clock = Clock.systemUTC();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final Object refreshLock = new Object();
    private volatile CachedIceServers cached;

    private record CachedIceServers(JsonNode servers, Instant fetchedAt) {
    }

    /**
     * Metered credentials are long-lived, so one fetch serves every call until the cache expires.
     * If a refresh fails, the previous configuration is reused rather than blocking new calls.
     */
    public JsonNode fetchIceServers() {
        CachedIceServers current = cached;
        if (isFresh(current)) {
            return current.servers().deepCopy();
        }

        synchronized (refreshLock) {
            current = cached;
            if (isFresh(current)) {
                return current.servers().deepCopy();
            }
            try {
                JsonNode servers = requestIceServers();
                cached = new CachedIceServers(servers, clock.instant());
                return servers.deepCopy();
            } catch (IllegalStateException exception) {
                if (current != null) {
                    LOGGER.warn("Refreshing TURN configuration failed; reusing the cached one", exception);
                    return current.servers().deepCopy();
                }
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), exception);
            }
        }
    }

    private boolean isFresh(CachedIceServers entry) {
        return entry != null
                && entry.fetchedAt().plusSeconds(Math.max(0L, cacheTtlSeconds)).isAfter(clock.instant());
    }

    /** Calls the provider; overridden in tests. */
    protected JsonNode requestIceServers() {
        if (apiKey == null || apiKey.isBlank() || credentialsUrl == null || credentialsUrl.isBlank()) {
            throw new IllegalStateException("TURN server chua duoc cau hinh");
        }

        String separator = credentialsUrl.contains("?") ? "&" : "?";
        String uri = credentialsUrl + separator + "apiKey=" +
                URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(uri))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("Khong lay duoc cau hinh TURN");
            }
            JsonNode body = objectMapper.readTree(response.body());
            if (!body.isArray() || body.isEmpty()) {
                throw new IllegalStateException("Metered tra ve cau hinh TURN khong hop le");
            }
            return body;
        } catch (IOException exception) {
            throw new IllegalStateException("Khong ket noi duoc toi TURN provider", exception);
        } catch (InterruptedException exception) {
            // Only restore the flag when this thread really was interrupted: a stray flag on a
            // pooled Tomcat thread would break the next request it serves.
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Bi gian doan khi lay cau hinh TURN", exception);
        }
    }
}
