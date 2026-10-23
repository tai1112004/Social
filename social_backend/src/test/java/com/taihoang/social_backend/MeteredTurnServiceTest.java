package com.taihoang.social_backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taihoang.social_backend.Service.MeteredTurnService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MeteredTurnServiceTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void credentialsAreFetchedOncePerCacheWindow() {
        FakeTurnService service = new FakeTurnService(servers("turn:a"), servers("turn:b"));
        service.setNow("2026-10-22T10:00:00Z");

        assertEquals("turn:a", firstUrl(service.fetchIceServers()));
        service.setNow("2026-10-22T10:04:59Z");
        assertEquals("turn:a", firstUrl(service.fetchIceServers()));
        assertEquals(1, service.requests);

        service.setNow("2026-10-22T10:05:01Z");
        assertEquals("turn:b", firstUrl(service.fetchIceServers()));
        assertEquals(2, service.requests);
    }

    @Test
    void failedRefreshReusesThePreviousConfiguration() {
        FakeTurnService service = new FakeTurnService(servers("turn:a"), () -> {
            throw new IllegalStateException("Metered down");
        });
        service.setNow("2026-10-22T10:00:00Z");
        service.fetchIceServers();

        service.setNow("2026-10-22T11:00:00Z");
        assertEquals("turn:a", firstUrl(service.fetchIceServers()));
    }

    @Test
    void failureWithoutAnyCachedConfigurationIsServiceUnavailable() {
        FakeTurnService service = new FakeTurnService(() -> {
            throw new IllegalStateException("TURN server chua duoc cau hinh");
        });

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, service::fetchIceServers);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void networkFailureDoesNotLeaveTheRequestThreadInterrupted() {
        MeteredTurnService service = new MeteredTurnService(MAPPER);
        ReflectionTestUtils.setField(service, "apiKey", "test-key");
        // Nothing listens on port 1, so the HTTP client fails with an IOException right away.
        ReflectionTestUtils.setField(service, "credentialsUrl", "http://127.0.0.1:1/api/v1/turn/credentials");

        assertThrows(ResponseStatusException.class, service::fetchIceServers);
        assertFalse(Thread.currentThread().isInterrupted());
    }

    private static Supplier<JsonNode> servers(String url) {
        return () -> MAPPER.createArrayNode().add(MAPPER.createObjectNode().put("urls", url));
    }

    private static String firstUrl(JsonNode servers) {
        return servers.get(0).get("urls").asText();
    }

    private static final class FakeTurnService extends MeteredTurnService {
        private final Deque<Supplier<JsonNode>> responses = new ArrayDeque<>();
        int requests;

        @SafeVarargs
        FakeTurnService(Supplier<JsonNode>... responses) {
            super(MAPPER);
            this.responses.addAll(java.util.List.of(responses));
        }

        void setNow(String instant) {
            ReflectionTestUtils.setField(this, MeteredTurnService.class, "clock",
                    Clock.fixed(Instant.parse(instant), ZoneOffset.UTC), Clock.class);
        }

        @Override
        protected JsonNode requestIceServers() {
            requests++;
            return responses.size() > 1 ? responses.poll().get() : responses.peek().get();
        }
    }
}
