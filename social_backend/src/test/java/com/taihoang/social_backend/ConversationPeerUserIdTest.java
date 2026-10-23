package com.taihoang.social_backend;

import com.taihoang.social_backend.Entity.Conversation_Member;
import com.taihoang.social_backend.Entity.Conversations;
import com.taihoang.social_backend.Entity.User;
import com.taihoang.social_backend.Repository.ConversationMemberRepository;
import com.taihoang.social_backend.Repository.ConversationRepository;
import com.taihoang.social_backend.Repository.MessengerRepository;
import com.taihoang.social_backend.Repository.MessengerStatusRepository;
import com.taihoang.social_backend.Service.Implements.ConversationQueryServiceIMPL;
import com.taihoang.social_backend.dto.ConversationListResponse;
import com.taihoang.social_backend.dto.ConversationSummaryResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConversationPeerUserIdTest {
    private static final long ME = 2L;

    @Test
    void privateChatsCarryTheOtherMembersIdEvenWhenNamesCollide() {
        // Three accounts named "vu": the chat list must identify each one by id.
        User me = user(ME, "tai2");
        Conversations chatWith32 = conversation(15L, Conversations.type_chat.private_chat);
        Conversations chatWith34 = conversation(19L, Conversations.type_chat.private_chat);
        Conversations group = conversation(11L, Conversations.type_chat.groups_chat);

        ConversationMemberRepository memberRepository = mock(ConversationMemberRepository.class);
        ConversationRepository conversationRepository = mock(ConversationRepository.class);
        MessengerRepository messengerRepository = mock(MessengerRepository.class);
        MessengerStatusRepository statusRepository = mock(MessengerStatusRepository.class);
        when(memberRepository.findConversationCursors(eq(ME), isNull(), isNull(), any()))
                .thenReturn(List.of(cursor(15L), cursor(19L), cursor(11L)));
        when(conversationRepository.findAllById(anyList())).thenReturn(List.of(chatWith32, chatWith34, group));
        when(messengerRepository.findAllById(anyList())).thenReturn(List.of());
        when(statusRepository.countUnreadByConversationIds(eq(ME), anyList())).thenReturn(List.of());
        when(memberRepository.findMembersByConversationIds(anyList())).thenReturn(List.of(
                member(chatWith32, me), member(chatWith32, user(32L, "vu")),
                member(chatWith34, me), member(chatWith34, user(34L, "vu")),
                member(group, me), member(group, user(32L, "vu")), member(group, user(33L, "vu"))
        ));

        ConversationListResponse response = new ConversationQueryServiceIMPL(
                memberRepository, conversationRepository, messengerRepository, statusRepository
        ).getConversations(ME, null, 20);

        Map<Long, ConversationSummaryResponse> byId = response.items().stream()
                .collect(Collectors.toMap(ConversationSummaryResponse::id, Function.identity()));
        assertEquals("vu", byId.get(15L).name());
        assertEquals(32L, byId.get(15L).peerUserId());
        assertEquals("vu", byId.get(19L).name());
        assertEquals(34L, byId.get(19L).peerUserId());
        assertNull(byId.get(11L).peerUserId());
    }

    private static ConversationMemberRepository.ConversationCursorView cursor(Long conversationId) {
        return new ConversationMemberRepository.ConversationCursorView() {
            @Override
            public Long getConversationId() {
                return conversationId;
            }

            @Override
            public Long getLastMessageId() {
                return null;
            }
        };
    }

    private static Conversations conversation(Long id, Conversations.type_chat type) {
        Conversations conversation = new Conversations();
        conversation.setId(id);
        conversation.setType(type);
        conversation.setName(type == Conversations.type_chat.groups_chat ? "Nhom" : null);
        return conversation;
    }

    private static Conversation_Member member(Conversations conversation, User user) {
        Conversation_Member member = new Conversation_Member();
        member.setConversation(conversation);
        member.setUser(user);
        return member;
    }

    private static User user(Long id, String userName) {
        User user = new User();
        user.setId(id);
        user.setUserName(userName);
        user.setEmail(userName + id + "@example.test");
        return user;
    }
}
