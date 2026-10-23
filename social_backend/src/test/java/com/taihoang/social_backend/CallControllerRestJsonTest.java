package com.taihoang.social_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taihoang.social_backend.Controllers.CallControllerRest;
import com.taihoang.social_backend.Entity.CallStatus;
import com.taihoang.social_backend.Entity.CallType;
import com.taihoang.social_backend.Entity.User;
import com.taihoang.social_backend.Service.CallService;
import com.taihoang.social_backend.Service.MeteredTurnService;
import com.taihoang.social_backend.dto.CallHistoryResponse;
import com.taihoang.social_backend.security.AuthenticatedUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Renders the call endpoints with the Jackson 3 converter Spring Boot 4 uses at runtime. A Jackson 2
 * JsonNode returned from a controller is written as a bean there ({"array":false,"nodeType":...}).
 */
class CallControllerRestJsonTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper jackson2 = new ObjectMapper().registerModule(new JavaTimeModule());
        ArrayNode servers = jackson2.createArrayNode();
        servers.addObject().putArray("urls").add("stun:stun.example.test:3478");
        servers.addObject().put("urls", "turn:turn.example.test:443").put("username", "u1").put("credential", "c1");

        MeteredTurnService meteredTurnService = mock(MeteredTurnService.class);
        when(meteredTurnService.fetchIceServers()).thenReturn(servers);
        CallService callService = mock(CallService.class);
        when(callService.history(7L, 50)).thenReturn(List.of(new CallHistoryResponse(
                3L, "call-3", 10L, 8L, "Callee", null, CallType.VIDEO, CallStatus.ENDED,
                LocalDateTime.parse("2026-10-22T10:00:00"), LocalDateTime.parse("2026-10-22T10:00:05"),
                LocalDateTime.parse("2026-10-22T10:01:05"), 60L, true
        )));

        mockMvc = MockMvcBuilders
                .standaloneSetup(new CallControllerRest(callService, meteredTurnService, jackson2))
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        User user = new User();
        user.setId(7L);
        user.setEmail("caller@example.test");
        AuthenticatedUserDetails details = new AuthenticatedUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void iceServersAreAnArrayOfServerObjects() throws Exception {
        mockMvc.perform(get("/user/calls/ice-servers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].urls[0]").value("stun:stun.example.test:3478"))
                .andExpect(jsonPath("$.items[1].urls").value("turn:turn.example.test:443"))
                .andExpect(jsonPath("$.items[1].username").value("u1"))
                .andExpect(jsonPath("$.items[1].credential").value("c1"))
                .andExpect(jsonPath("$.nodeType").doesNotExist());
    }

    @Test
    void historyItemsUseTheRecordFieldNames() throws Exception {
        mockMvc.perform(get("/user/calls/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].callId").value("call-3"))
                .andExpect(jsonPath("$.items[0].otherUserName").value("Callee"))
                .andExpect(jsonPath("$.items[0].status").value("ENDED"))
                .andExpect(jsonPath("$.items[0].callType").value("VIDEO"))
                .andExpect(jsonPath("$.items[0].createdAt").value("2026-10-22T10:00:00"))
                .andExpect(jsonPath("$.items[0].durationSeconds").value(60))
                .andExpect(jsonPath("$.items[0].outgoing").value(true));
    }
}
