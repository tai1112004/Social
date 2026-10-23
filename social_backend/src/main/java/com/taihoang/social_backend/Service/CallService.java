package com.taihoang.social_backend.Service;

import com.taihoang.social_backend.Entity.*;
import com.taihoang.social_backend.Repository.CallHistoryRepository;
import com.taihoang.social_backend.Repository.ConversationMemberRepository;
import com.taihoang.social_backend.Repository.ConversationRepository;
import com.taihoang.social_backend.Repository.UserRepository;
import com.taihoang.social_backend.dto.CallEvent;
import com.taihoang.social_backend.dto.CallHistoryResponse;
import com.taihoang.social_backend.dto.CallInviteRequest;
import com.taihoang.social_backend.exception.CallInviteException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class CallService {
    private final CallHistoryRepository callHistoryRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final UserRepository userRepository;

    /** A user in one of these calls cannot start or receive another. */
    private static final List<CallStatus> ACTIVE_STATUSES = List.of(CallStatus.RINGING, CallStatus.ACCEPTED);

    @Value("${app.calls.ringing-timeout-seconds:30}")
    private long ringingTimeoutSeconds = 30L;

    @Value("${app.calls.heartbeat-timeout-seconds:45}")
    private long heartbeatTimeoutSeconds = 45L;

    /**
     * Returns CALL_INVITE for both users, or CALL_BUSY (for the caller only) when the callee is
     * already in a call. Invites that cannot start at all throw {@link CallInviteException}.
     */
    @Transactional
    public CallEvent invite(Long callerId, CallInviteRequest request) {
        Conversations conversation = conversationRepository.findById(request.conversationId())
                .orElseThrow(() -> new CallInviteException("Khong tim thay conversation"));
        if (conversation.getType() != Conversations.type_chat.private_chat) {
            throw new CallInviteException("Chi ho tro goi 1-1 o giai doan nay");
        }
        if (!isMember(conversation.getId(), callerId) || !isMember(conversation.getId(), request.calleeId())) {
            throw new CallInviteException("User khong thuoc conversation nay");
        }
        if (callerId.equals(request.calleeId())) {
            throw new CallInviteException("Khong the tu goi cho chinh minh");
        }

        // Lock both users in a fixed order so two concurrent invites involving the same person
        // cannot both pass the busy check below.
        User first = lockUser(Math.min(callerId, request.calleeId()));
        User second = lockUser(Math.max(callerId, request.calleeId()));
        User caller = first.getId().equals(callerId) ? first : second;
        User callee = caller == first ? second : first;

        if (callHistoryRepository.existsForUserWithStatusIn(callerId, ACTIVE_STATUSES)) {
            throw new CallInviteException("Ban dang co mot cuoc goi chua ket thuc");
        }
        boolean calleeBusy = callHistoryRepository.existsForUserWithStatusIn(callee.getId(), ACTIVE_STATUSES);

        LocalDateTime now = LocalDateTime.now();
        CallHistory history = new CallHistory();
        history.setConversation(conversation);
        history.setCaller(caller);
        history.setCallee(callee);
        history.setCallType(request.callType());
        history.setCreatedAt(now);
        history.setLastActivityAt(now);
        if (calleeBusy) {
            history.setStatus(CallStatus.BUSY);
            history.setEndedAt(now);
            history.setFailureReason("Callee is in another call");
        } else {
            history.setStatus(CallStatus.RINGING);
        }
        callHistoryRepository.save(history);
        return event(calleeBusy ? "CALL_BUSY" : "CALL_INVITE", history, callerId, null);
    }

    /**
     * Ends ACCEPTED calls whose clients stopped sending heartbeats or signals, e.g. both apps were
     * killed. Without this such a call would stay ACCEPTED and keep both users "busy" forever.
     */
    @Transactional
    public List<CallEvent> expireStaleCalls() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(Math.max(1L, heartbeatTimeoutSeconds));
        return expireStaleCalls(cutoff);
    }

    @Transactional
    public List<CallEvent> expireStaleCalls(LocalDateTime cutoff) {
        return callHistoryRepository.findInactiveForUpdate(CallStatus.ACCEPTED, cutoff).stream()
                .map(history -> {
                    finish(history, CallStatus.FAILED, null);
                    history.setFailureReason("No call heartbeat before timeout");
                    callHistoryRepository.save(history);
                    return event("CALL_FAILED", history, null, null);
                })
                .toList();
    }

    @Transactional
    public CallEvent action(Long actorId, String type, String callId, JsonNode payload) {
        CallHistory history = callHistoryRepository.findByCallIdForUpdate(callId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay cuoc goi"));
        if (!actorId.equals(history.getCaller().getId()) && !actorId.equals(history.getCallee().getId())) {
            throw new IllegalArgumentException("User khong thuoc cuoc goi");
        }

        CallStatus status = history.getStatus();
        switch (type) {
            case "CALL_ACCEPT" -> {
                requireActor(actorId, history.getCallee().getId());
                if (status != CallStatus.RINGING) throw new IllegalArgumentException("Cuoc goi khong con dang cho");
                history.setStatus(CallStatus.ACCEPTED);
                history.setAnsweredAt(LocalDateTime.now());
            }
            case "CALL_REJECT" -> {
                requireActor(actorId, history.getCallee().getId());
                finish(history, CallStatus.REJECTED, actorId);
            }
            case "CALL_CANCEL" -> {
                requireActor(actorId, history.getCaller().getId());
                finish(history, CallStatus.CANCELLED, actorId);
            }
            case "CALL_END" -> finish(history, CallStatus.ENDED, actorId);
            case "CALL_FAILED" -> finish(history, CallStatus.FAILED, actorId);
            case "CALL_OFFER", "CALL_ANSWER", "CALL_ICE_CANDIDATE", "CALL_RECONNECT" -> {
                if (status != CallStatus.RINGING && status != CallStatus.ACCEPTED) {
                    throw new IllegalArgumentException("Cuoc goi khong o trang thai ket noi");
                }
            }
            default -> throw new IllegalArgumentException("Loai su kien cuoc goi khong hop le");
        }
        history.setLastActivityAt(LocalDateTime.now());
        callHistoryRepository.save(history);
        return event(type, history, actorId, payload);
    }

    /** Keeps an ongoing call alive; returns false, without failing, once the call has ended. */
    @Transactional
    public boolean heartbeat(Long actorId, String callId) {
        return callHistoryRepository.findByCallIdForUpdate(callId)
                .filter(history -> actorId.equals(history.getCaller().getId()) || actorId.equals(history.getCallee().getId()))
                .filter(history -> ACTIVE_STATUSES.contains(history.getStatus()))
                .map(history -> {
                    history.setLastActivityAt(LocalDateTime.now());
                    callHistoryRepository.save(history);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public List<CallEvent> expireRingingCalls() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(Math.max(1L, ringingTimeoutSeconds));
        return expireRingingCalls(cutoff);
    }

    /**
     * Kept with an explicit cutoff so expiry behavior can be tested without waiting in real time.
     */
    @Transactional
    public List<CallEvent> expireRingingCalls(LocalDateTime cutoff) {
        return callHistoryRepository.findByStatusAndCreatedAtBefore(CallStatus.RINGING, cutoff).stream()
                .map(history -> {
                    history.setStatus(CallStatus.MISSED);
                    history.setEndedAt(LocalDateTime.now());
                    history.setEndedBy(null);
                    history.setFailureReason("No answer before ringing timeout");
                    callHistoryRepository.save(history);
                    return event("CALL_MISSED", history, null, null);
                })
                .toList();
    }

    @Transactional
    public List<CallHistoryResponse> history(Long userId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return callHistoryRepository.findForUser(userId, PageRequest.of(0, safeLimit)).stream()
                .map(call -> {
                    boolean outgoing = call.getCaller().getId().equals(userId);
                    User other = outgoing ? call.getCallee() : call.getCaller();
                    return new CallHistoryResponse(
                            call.getId(), call.getCallId(), call.getConversation().getId(),
                            other.getId(), other.getUserName(), other.getAvatarUrl(),
                            call.getCallType(), call.getStatus(), call.getCreatedAt(),
                            call.getAnsweredAt(), call.getEndedAt(), call.getDurationSeconds(), outgoing
                    );
                })
                .toList();
    }

    private void finish(CallHistory history, CallStatus status, Long actorId) {
        if (history.getStatus() == CallStatus.ENDED || history.getStatus() == CallStatus.REJECTED ||
                history.getStatus() == CallStatus.CANCELLED || history.getStatus() == CallStatus.MISSED ||
                history.getStatus() == CallStatus.FAILED || history.getStatus() == CallStatus.BUSY) {
            throw new IllegalArgumentException("Cuoc goi da ket thuc");
        }
        LocalDateTime endedAt = LocalDateTime.now();
        history.setStatus(status);
        history.setEndedAt(endedAt);
        history.setEndedBy(actorId);
        if (history.getAnsweredAt() != null) {
            history.setDurationSeconds(Math.max(0, java.time.Duration.between(history.getAnsweredAt(), endedAt).toSeconds()));
        }
    }

    private boolean isMember(Long conversationId, Long userId) {
        return conversationMemberRepository.existsByConversationIdAndUserId(conversationId, userId);
    }

    private User lockUser(Long userId) {
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new CallInviteException("Khong tim thay user " + userId));
    }

    private void requireActor(Long actual, Long expected) {
        if (!actual.equals(expected)) throw new IllegalArgumentException("User khong co quyen thao tac cuoc goi");
    }

    private CallEvent event(String type, CallHistory history, Long actorId, JsonNode payload) {
        return new CallEvent(type, history.getCallId(), history.getConversation().getId(),
                history.getCaller().getId(), history.getCaller().getUserName(),
                history.getCallee().getId(), history.getCallee().getUserName(),
                history.getCallType(), history.getStatus(), actorId, payload, LocalDateTime.now());
    }
}
