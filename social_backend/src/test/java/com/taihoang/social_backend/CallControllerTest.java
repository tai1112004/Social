package com.taihoang.social_backend;

import com.taihoang.social_backend.Entity.CallStatus;
import com.taihoang.social_backend.Entity.CallType;
import com.taihoang.social_backend.Entity.User;
import com.taihoang.social_backend.Repository.UserRepository;
import com.taihoang.social_backend.Service.CallService;
import com.taihoang.social_backend.configure.CallController;
import com.taihoang.social_backend.dto.CallEvent;
import com.taihoang.social_backend.dto.CallInviteRequest;
import com.taihoang.social_backend.dto.CallSignalRequest;
import com.taihoang.social_backend.security.AuthenticatedUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CallControllerTest {
    private CallService callService;
    private SimpMessagingTemplate messagingTemplate;
    private CallController controller;
    private Principal caller;

    @BeforeEach
    void setUp() {
        callService = mock(CallService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "caller@test")));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, "callee@test")));
        controller = new CallController(callService, messagingTemplate, userRepository);
        AuthenticatedUserDetails details = new AuthenticatedUserDetails(user(1L, "caller@test"));
        caller = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
    }

    @Test
    void ringingInviteReachesBothUsers() {
        when(callService.invite(eq(1L), any())).thenReturn(event("CALL_INVITE", CallStatus.RINGING));

        controller.invite(new CallInviteRequest(10L, 2L, CallType.AUDIO), caller);

        verify(messagingTemplate).convertAndSendToUser(eq("caller@test"), eq("/queue/calls"), any());
        verify(messagingTemplate).convertAndSendToUser(eq("callee@test"), eq("/queue/calls"), any());
    }

    @Test
    void busyResultOnlyGoesBackToTheCaller() {
        when(callService.invite(eq(1L), any())).thenReturn(event("CALL_BUSY", CallStatus.BUSY));

        controller.invite(new CallInviteRequest(10L, 2L, CallType.AUDIO), caller);

        verify(messagingTemplate).convertAndSendToUser(eq("caller@test"), eq("/queue/calls"), any());
        verify(messagingTemplate, never()).convertAndSendToUser(eq("callee@test"), anyString(), any());
    }

    @Test
    void heartbeatIsNotRelayed() {
        controller.heartbeat(new CallSignalRequest("call-1", null), caller);

        verify(callService).heartbeat(1L, "call-1");
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void staleCallsAreReportedToBothUsers() {
        when(callService.expireRingingCalls()).thenReturn(List.of());
        when(callService.expireStaleCalls()).thenReturn(List.of(event("CALL_FAILED", CallStatus.FAILED)));

        controller.expireMissedCalls();

        verify(messagingTemplate).convertAndSendToUser(eq("caller@test"), eq("/queue/calls"), any());
        verify(messagingTemplate).convertAndSendToUser(eq("callee@test"), eq("/queue/calls"), any());
    }

    private static CallEvent event(String type, CallStatus status) {
        return new CallEvent(type, "call-1", 10L, 1L, "caller", 2L, "callee",
                CallType.AUDIO, status, 1L, null, LocalDateTime.now());
    }

    private static User user(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setUserName(email.substring(0, email.indexOf('@')));
        return user;
    }
}
