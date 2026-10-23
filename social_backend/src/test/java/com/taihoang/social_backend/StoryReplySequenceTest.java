package com.taihoang.social_backend;

import com.taihoang.social_backend.Entity.Conversations;
import com.taihoang.social_backend.Entity.Messenger;
import com.taihoang.social_backend.Entity.Story;
import com.taihoang.social_backend.Entity.User;
import com.taihoang.social_backend.Repository.ConversationMemberRepository;
import com.taihoang.social_backend.Repository.ConversationRepository;
import com.taihoang.social_backend.Repository.MessengerRepository;
import com.taihoang.social_backend.Repository.MessengerStatusRepository;
import com.taihoang.social_backend.Repository.StoryRepository;
import com.taihoang.social_backend.Repository.StoryViewRepository;
import com.taihoang.social_backend.Repository.UserRepository;
import com.taihoang.social_backend.Service.StoryAccessService;
import com.taihoang.social_backend.Service.StoryMessageReferenceMapper;
import com.taihoang.social_backend.Service.Implements.StoryReplyServiceImpl;
import com.taihoang.social_backend.dto.StoryReplyRequest;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StoryReplySequenceTest {
    private static final long AUTHOR_ID = 28L;
    private static final long SENDER_ID = 29L;
    private static final long STORY_ID = 7L;

    private final StoryRepository storyRepository = mock(StoryRepository.class);
    private final StoryViewRepository storyViewRepository = mock(StoryViewRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ConversationRepository conversationRepository = mock(ConversationRepository.class);
    private final ConversationMemberRepository memberRepository = mock(ConversationMemberRepository.class);
    private final MessengerRepository messengerRepository = mock(MessengerRepository.class);
    private final MessengerStatusRepository statusRepository = mock(MessengerStatusRepository.class);

    private final StoryReplyServiceImpl service = new StoryReplyServiceImpl(
            storyRepository, storyViewRepository, userRepository, conversationRepository, memberRepository,
            messengerRepository, statusRepository, mock(StoryAccessService.class),
            mock(StoryMessageReferenceMapper.class), mock(SimpMessagingTemplate.class)
    );
    private final Conversations chat = conversation(15L);

    @BeforeEach
    void setUp() {
        User author = user(AUTHOR_ID);
        Story story = new Story();
        story.setId(STORY_ID);
        story.setAuthor(author);
        when(storyRepository.findActiveById(eq(STORY_ID), any())).thenReturn(Optional.of(story));
        when(userRepository.findById(SENDER_ID)).thenReturn(Optional.of(user(SENDER_ID)));
        when(userRepository.findByIdForUpdate(anyLong()))
                .thenAnswer(invocation -> Optional.of(user(invocation.getArgument(0))));
        when(conversationRepository.findByIdForUpdate(15L)).thenReturn(Optional.of(chat));
        when(messengerRepository.findByConversationIdAndUserIdAndClientMessageId(15L, SENDER_ID, "c-1"))
                .thenReturn(Optional.empty());
        when(messengerRepository.findMaxSequenceNumberForUpdate(15L)).thenReturn(41L);
        when(messengerRepository.save(any(Messenger.class))).thenAnswer(invocation -> {
            Messenger message = invocation.getArgument(0);
            message.setId(500L);
            return message;
        });
    }

    @Test
    void replyTakesTheNextSequenceOnlyAfterLockingTheConversation() {
        when(conversationRepository.findDirectConversations(any(), eq(SENDER_ID), eq(AUTHOR_ID), any()))
                .thenReturn(List.of(chat));

        assertEquals(42L, service.replyToStory(SENDER_ID, STORY_ID, new StoryReplyRequest("c-1", "hay qua")).sequenceNumber());

        InOrder order = inOrder(conversationRepository, messengerRepository);
        order.verify(conversationRepository).findByIdForUpdate(15L);
        order.verify(messengerRepository).findByConversationIdAndUserIdAndClientMessageId(15L, SENDER_ID, "c-1");
        order.verify(messengerRepository).findMaxSequenceNumberForUpdate(15L);
        // An existing chat needs no user locks.
        verify(userRepository, never()).findByIdForUpdate(anyLong());
        // Several quick replies must not collide on the story_view unique key.
        verify(storyViewRepository).markViewed(eq(STORY_ID), eq(SENDER_ID), any());
        verify(storyViewRepository, never()).save(any());
    }

    @Test
    void firstReplyLocksBothUsersInIdOrderAndReusesAChatCreatedWhileWaiting() {
        // Nothing before the locks, then the chat another request committed while this one waited.
        when(conversationRepository.findDirectConversations(any(), eq(SENDER_ID), eq(AUTHOR_ID), any()))
                .thenReturn(List.of())
                .thenReturn(List.of(chat));

        service.replyToStory(SENDER_ID, STORY_ID, new StoryReplyRequest("c-1", "hay qua"));

        InOrder order = inOrder(userRepository, conversationRepository);
        order.verify(userRepository).findByIdForUpdate(AUTHOR_ID);
        order.verify(userRepository).findByIdForUpdate(SENDER_ID);
        order.verify(conversationRepository).findDirectConversations(any(), eq(SENDER_ID), eq(AUTHOR_ID), any());
        verify(conversationRepository, never()).save(any());
        ArgumentCaptor<Messenger> saved = ArgumentCaptor.forClass(Messenger.class);
        verify(messengerRepository).save(saved.capture());
        assertEquals(15L, saved.getValue().getConversation().getId());
    }

    @Test
    void replyRunsAtReadCommittedSoReadsAfterALockSeeCommittedRows() throws NoSuchMethodException {
        Transactional transactional = StoryReplyServiceImpl.class
                .getMethod("replyToStory", Long.class, Long.class, StoryReplyRequest.class)
                .getAnnotation(Transactional.class);

        assertEquals(Isolation.READ_COMMITTED, transactional.isolation());
    }

    @Test
    void databaseRejectsTwoMessagesWithTheSameSequenceInOneChat() {
        UniqueConstraint[] constraints = Messenger.class.getAnnotation(Table.class).uniqueConstraints();

        assertTrue(Arrays.stream(constraints).anyMatch(constraint ->
                List.of(constraint.columnNames()).equals(List.of("conversation_id", "sequence_number"))));
    }

    private static Conversations conversation(Long id) {
        Conversations conversation = new Conversations();
        conversation.setId(id);
        conversation.setType(Conversations.type_chat.private_chat);
        return conversation;
    }

    private static User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setUserName("user" + id);
        user.setEmail("user" + id + "@example.test");
        return user;
    }
}
