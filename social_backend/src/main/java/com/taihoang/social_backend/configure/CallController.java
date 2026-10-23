package com.taihoang.social_backend.configure;

import com.taihoang.social_backend.Repository.UserRepository;
import com.taihoang.social_backend.Service.CallService;
import com.taihoang.social_backend.dto.CallEvent;
import com.taihoang.social_backend.dto.CallInviteRequest;
import com.taihoang.social_backend.dto.CallSignalRequest;
import com.taihoang.social_backend.security.AuthenticatedUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.scheduling.annotation.Scheduled;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class CallController {
    private final CallService callService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @MessageMapping("/call.invite")
    public void invite(@Valid @Payload CallInviteRequest request, Principal principal) {
        Long actorId = extractUserId(principal);
        CallEvent event = callService.invite(actorId, request);
        send(event, event.callerId(), "/queue/calls");
        // A busy callee is not disturbed; only the caller learns the call could not ring.
        if (!"CALL_BUSY".equals(event.type())) {
            send(event, event.calleeId(), "/queue/calls");
        }
    }

    @MessageMapping("/call.heartbeat")
    public void heartbeat(@Valid @Payload CallSignalRequest request, Principal principal) {
        // Not relayed: it only proves to the server that this participant is still in the call.
        callService.heartbeat(extractUserId(principal), request.callId());
    }

    @MessageMapping("/call.accept")
    public void accept(@Valid @Payload CallSignalRequest request, Principal principal) { relay("CALL_ACCEPT", request, principal); }

    @MessageMapping("/call.reject")
    public void reject(@Valid @Payload CallSignalRequest request, Principal principal) { relay("CALL_REJECT", request, principal); }

    @MessageMapping("/call.cancel")
    public void cancel(@Valid @Payload CallSignalRequest request, Principal principal) { relay("CALL_CANCEL", request, principal); }

    @MessageMapping("/call.end")
    public void end(@Valid @Payload CallSignalRequest request, Principal principal) { relay("CALL_END", request, principal); }

    @MessageMapping("/call.failed")
    public void failed(@Valid @Payload CallSignalRequest request, Principal principal) { relay("CALL_FAILED", request, principal); }

    @MessageMapping("/call.offer")
    public void offer(@Valid @Payload CallSignalRequest request, Principal principal) { relay("CALL_OFFER", request, principal); }

    @MessageMapping("/call.answer")
    public void answer(@Valid @Payload CallSignalRequest request, Principal principal) { relay("CALL_ANSWER", request, principal); }

    @MessageMapping("/call.ice")
    public void ice(@Valid @Payload CallSignalRequest request, Principal principal) { relay("CALL_ICE_CANDIDATE", request, principal); }

    @MessageMapping("/call.reconnect")
    public void reconnect(@Valid @Payload CallSignalRequest request, Principal principal) { relay("CALL_RECONNECT", request, principal); }

    @Scheduled(
            fixedDelayString = "${app.calls.expiry-scan-ms:5000}",
            initialDelayString = "${app.calls.expiry-initial-delay-ms:5000}"
    )
    public void expireMissedCalls() {
        callService.expireRingingCalls().forEach(event -> {
            send(event, event.callerId(), "/queue/calls");
            send(event, event.calleeId(), "/queue/calls");
        });
        callService.expireStaleCalls().forEach(event -> {
            send(event, event.callerId(), "/queue/calls");
            send(event, event.calleeId(), "/queue/calls");
        });
    }

    private void relay(String type, CallSignalRequest request, Principal principal) {
        Long actorId = extractUserId(principal);
        CallEvent event = callService.action(actorId, type, request.callId(), request.payload());
        Long targetId = event.callerId().equals(actorId) ? event.calleeId() : event.callerId();
        send(event, targetId, "/queue/calls");
        if (type.equals("CALL_ACCEPT") || type.equals("CALL_REJECT") || type.equals("CALL_CANCEL") ||
                type.equals("CALL_END") || type.equals("CALL_FAILED")) {
            send(event, actorId, "/queue/calls");
        }
    }

    private void send(CallEvent event, Long userId, String destination) {
        userRepository.findById(userId)
                .map(user -> user.getEmail())
                .ifPresent(email -> messagingTemplate.convertAndSendToUser(email, destination, event));
    }

    private Long extractUserId(Principal principal) {
        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof AuthenticatedUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new IllegalArgumentException("Khong xac dinh duoc user cuoc goi");
    }
}
