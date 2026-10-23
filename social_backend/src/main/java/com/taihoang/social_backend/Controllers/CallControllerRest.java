package com.taihoang.social_backend.Controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taihoang.social_backend.Service.CallService;
import com.taihoang.social_backend.Service.MeteredTurnService;
import com.taihoang.social_backend.dto.CallHistoryResponse;
import com.taihoang.social_backend.security.AuthenticatedUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/calls")
@RequiredArgsConstructor
public class CallControllerRest {
    private final CallService callService;
    private final MeteredTurnService meteredTurnService;
    private final ObjectMapper objectMapper;

    // Responses are plain maps/lists/records: Spring Boot 4 writes them with Jackson 3, which
    // serializes a Jackson 2 JsonNode as a bean ({"array":false,"nodeType":...}) instead of its content.

    @GetMapping("/ice-servers")
    public Map<String, List<Object>> iceServers(@AuthenticationPrincipal AuthenticatedUserDetails currentUser) {
        requireUser(currentUser);
        List<Object> servers = objectMapper.convertValue(
                meteredTurnService.fetchIceServers(),
                new TypeReference<List<Object>>() { }
        );
        return Map.of("items", servers);
    }

    @GetMapping("/history")
    public Map<String, List<CallHistoryResponse>> history(
            @AuthenticationPrincipal AuthenticatedUserDetails currentUser,
            @RequestParam(defaultValue = "50") int limit
    ) {
        requireUser(currentUser);
        return Map.of("items", callService.history(currentUser.getId(), limit));
    }

    private void requireUser(AuthenticatedUserDetails currentUser) {
        if (currentUser == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chua dang nhap");
    }
}
