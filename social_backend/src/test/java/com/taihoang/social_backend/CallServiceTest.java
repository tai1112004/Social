package com.taihoang.social_backend;

import com.taihoang.social_backend.Entity.CallStatus;
import com.taihoang.social_backend.Entity.CallType;
import com.taihoang.social_backend.Entity.CallHistory;
import com.taihoang.social_backend.Entity.Conversations;
import com.taihoang.social_backend.Entity.User;
import com.taihoang.social_backend.Repository.CallHistoryRepository;
import com.taihoang.social_backend.Repository.ConversationMemberRepository;
import com.taihoang.social_backend.Repository.ConversationRepository;
import com.taihoang.social_backend.Repository.UserRepository;
import com.taihoang.social_backend.Service.CallService;
import com.taihoang.social_backend.dto.CallEvent;
import com.taihoang.social_backend.dto.CallInviteRequest;
import com.taihoang.social_backend.exception.CallInviteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CallServiceTest {
    private CallHistoryRepository historyRepository;
    private ConversationRepository conversationRepository;
    private ConversationMemberRepository memberRepository;
    private UserRepository userRepository;
    private CallService callService;

    @BeforeEach
    void setUp() {
        historyRepository = mock(CallHistoryRepository.class);
        conversationRepository = mock(ConversationRepository.class);
        memberRepository = mock(ConversationMemberRepository.class);
        userRepository = mock(UserRepository.class);
        callService = new CallService(historyRepository, conversationRepository, memberRepository, userRepository);
    }

    @Test
    void inviteRequiresBothUsersToBelongToTheDirectConversation() {
        Conversations conversation = conversation(10L, Conversations.type_chat.private_chat);
        when(conversationRepository.findById(10L)).thenReturn(Optional.of(conversation));
        when(memberRepository.existsByConversationIdAndUserId(10L, 1L)).thenReturn(true);
        when(memberRepository.existsByConversationIdAndUserId(10L, 2L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> callService.invite(
                1L,
                new CallInviteRequest(10L, 2L, CallType.AUDIO)
        ));
    }

    @Test
    void inviteCreatesRingingHistoryAndEvent() {
        Conversations conversation = conversation(10L, Conversations.type_chat.private_chat);
        User caller = user(1L, "caller@test");
        User callee = user(2L, "callee@test");
        when(conversationRepository.findById(10L)).thenReturn(Optional.of(conversation));
        when(memberRepository.existsByConversationIdAndUserId(10L, 1L)).thenReturn(true);
        when(memberRepository.existsByConversationIdAndUserId(10L, 2L)).thenReturn(true);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(caller));
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(callee));
        when(historyRepository.save(any(CallHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CallEvent event = callService.invite(1L, new CallInviteRequest(10L, 2L, CallType.VIDEO));

        assertEquals("CALL_INVITE", event.type());
        assertEquals(CallStatus.RINGING, event.status());
        assertEquals(CallType.VIDEO, event.callType());
        assertEquals(1L, event.callerId());
        assertEquals(2L, event.calleeId());
        verify(historyRepository).save(any(CallHistory.class));
    }

    @Test
    void acceptOnlyAllowsTheCalleeAndChangesStatus() {
        CallHistory history = ringingHistory();
        when(historyRepository.findByCallIdForUpdate("call-1")).thenReturn(Optional.of(history));
        when(historyRepository.save(any(CallHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CallEvent event = callService.action(2L, "CALL_ACCEPT", "call-1", null);

        assertEquals(CallStatus.ACCEPTED, history.getStatus());
        assertEquals(CallStatus.ACCEPTED, event.status());
        assertEquals(2L, event.actorId());
    }

    @Test
    void callerCannotAcceptTheirOwnIncomingCall() {
        CallHistory history = ringingHistory();
        when(historyRepository.findByCallIdForUpdate("call-1")).thenReturn(Optional.of(history));

        assertThrows(IllegalArgumentException.class, () -> callService.action(1L, "CALL_ACCEPT", "call-1", null));
    }

    @Test
    void expiryMarksOldRingingCallAsMissed() {
        CallHistory history = ringingHistory();
        history.setCreatedAt(LocalDateTime.now().minusSeconds(31));
        when(historyRepository.findByStatusAndCreatedAtBefore(any(), any())).thenReturn(List.of(history));
        when(historyRepository.save(any(CallHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<CallEvent> events = callService.expireRingingCalls(LocalDateTime.now());

        assertEquals(CallStatus.MISSED, history.getStatus());
        assertEquals("CALL_MISSED", events.get(0).type());
        assertEquals(CallStatus.MISSED, events.get(0).status());
        verify(historyRepository).save(history);
    }

    @Test
    void inviteToBusyCalleeIsRecordedAsBusyWithoutRinging() {
        stubDirectConversation();
        when(historyRepository.existsForUserWithStatusIn(eq(2L), any())).thenReturn(true);
        ArgumentCaptor<CallHistory> saved = ArgumentCaptor.forClass(CallHistory.class);
        when(historyRepository.save(saved.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        CallEvent event = callService.invite(1L, new CallInviteRequest(10L, 2L, CallType.AUDIO));

        assertEquals("CALL_BUSY", event.type());
        assertEquals(CallStatus.BUSY, saved.getValue().getStatus());
        assertNotNull(saved.getValue().getEndedAt());
    }

    @Test
    void callerWithUnfinishedCallCannotStartAnother() {
        stubDirectConversation();
        when(historyRepository.existsForUserWithStatusIn(eq(1L), any())).thenReturn(true);

        assertThrows(CallInviteException.class, () -> callService.invite(1L, new CallInviteRequest(10L, 2L, CallType.AUDIO)));
        verify(historyRepository, never()).save(any(CallHistory.class));
    }

    @Test
    void usersAreLockedInIdOrderWhicheverSideCalls() {
        stubDirectConversation();
        when(historyRepository.save(any(CallHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        callService.invite(2L, new CallInviteRequest(10L, 1L, CallType.AUDIO));

        InOrder order = inOrder(userRepository);
        order.verify(userRepository).findByIdForUpdate(1L);
        order.verify(userRepository).findByIdForUpdate(2L);
    }

    @Test
    void signalingRefreshesLastActivity() {
        CallHistory history = ringingHistory();
        history.setStatus(CallStatus.ACCEPTED);
        history.setLastActivityAt(LocalDateTime.now().minusMinutes(5));
        when(historyRepository.findByCallIdForUpdate("call-1")).thenReturn(Optional.of(history));

        callService.action(1L, "CALL_ICE_CANDIDATE", "call-1", null);

        assertTrue(history.getLastActivityAt().isAfter(LocalDateTime.now().minusSeconds(5)));
    }

    @Test
    void heartbeatKeepsOngoingCallAliveAndIgnoresEndedOnes() {
        CallHistory ongoing = ringingHistory();
        ongoing.setStatus(CallStatus.ACCEPTED);
        ongoing.setLastActivityAt(LocalDateTime.now().minusMinutes(5));
        when(historyRepository.findByCallIdForUpdate("call-1")).thenReturn(Optional.of(ongoing));

        assertTrue(callService.heartbeat(2L, "call-1"));
        assertTrue(ongoing.getLastActivityAt().isAfter(LocalDateTime.now().minusSeconds(5)));
        assertFalse(callService.heartbeat(99L, "call-1"));

        ongoing.setStatus(CallStatus.ENDED);
        assertFalse(callService.heartbeat(2L, "call-1"));
    }

    @Test
    void acceptedCallWithoutHeartbeatIsEndedAsFailed() {
        CallHistory stale = ringingHistory();
        stale.setStatus(CallStatus.ACCEPTED);
        stale.setAnsweredAt(LocalDateTime.now().minusMinutes(3));
        stale.setLastActivityAt(LocalDateTime.now().minusMinutes(2));
        when(historyRepository.findInactiveForUpdate(eq(CallStatus.ACCEPTED), any())).thenReturn(List.of(stale));

        List<CallEvent> events = callService.expireStaleCalls(LocalDateTime.now().minusSeconds(45));

        assertEquals(CallStatus.FAILED, stale.getStatus());
        assertNotNull(stale.getEndedAt());
        assertTrue(stale.getDurationSeconds() >= 179);
        assertEquals("CALL_FAILED", events.get(0).type());
        verify(historyRepository).save(stale);
    }

    @Test
    void busyCallCannotBeEndedAgain() {
        CallHistory busy = ringingHistory();
        busy.setStatus(CallStatus.BUSY);
        when(historyRepository.findByCallIdForUpdate("call-1")).thenReturn(Optional.of(busy));

        assertThrows(IllegalArgumentException.class, () -> callService.action(1L, "CALL_END", "call-1", null));
    }

    private void stubDirectConversation() {
        when(conversationRepository.findById(10L)).thenReturn(Optional.of(conversation(10L, Conversations.type_chat.private_chat)));
        when(memberRepository.existsByConversationIdAndUserId(10L, 1L)).thenReturn(true);
        when(memberRepository.existsByConversationIdAndUserId(10L, 2L)).thenReturn(true);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user(1L, "caller@test")));
        when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(user(2L, "callee@test")));
    }

    private static CallHistory ringingHistory() {
        CallHistory history = new CallHistory();
        history.setCallId("call-1");
        history.setStatus(CallStatus.RINGING);
        history.setCallType(CallType.AUDIO);
        history.setConversation(conversation(10L, Conversations.type_chat.private_chat));
        history.setCaller(user(1L, "caller@test"));
        history.setCallee(user(2L, "callee@test"));
        return history;
    }

    private static Conversations conversation(Long id, Conversations.type_chat type) {
        Conversations conversation = new Conversations();
        conversation.setId(id);
        conversation.setType(type);
        return conversation;
    }

    private static User user(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setUserName(email.substring(0, email.indexOf('@')));
        return user;
    }
}
