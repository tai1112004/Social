package com.taihoang.social_backend;

import com.taihoang.social_backend.Entity.Conversations;
import com.taihoang.social_backend.Entity.Messenger;
import com.taihoang.social_backend.Entity.MessengerStatus;
import com.taihoang.social_backend.Entity.User;
import com.taihoang.social_backend.Repository.ChatUploadRepository;
import com.taihoang.social_backend.Repository.ConversationMemberRepository;
import com.taihoang.social_backend.Repository.ConversationRepository;
import com.taihoang.social_backend.Repository.MessageAttachmentRepository;
import com.taihoang.social_backend.Repository.MessageReactionRepository;
import com.taihoang.social_backend.Repository.MessageUserStateRepository;
import com.taihoang.social_backend.Repository.MessengerRepository;
import com.taihoang.social_backend.Repository.MessengerStatusRepository;
import com.taihoang.social_backend.Repository.UserRepository;
import com.taihoang.social_backend.Service.ChatObservabilityService;
import com.taihoang.social_backend.Service.ChatRateLimiterService;
import com.taihoang.social_backend.Service.MessageService;
import com.taihoang.social_backend.dto.EditMessageRequest;
import com.taihoang.social_backend.dto.SeenConversationReceipt;
import com.taihoang.social_backend.dto.SeenConversationRequest;
import com.taihoang.social_backend.dto.SeenConversationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageServiceTest {

    private static final long CONVERSATION_ID = 50L;

    private UserRepository userRepository;
    private ConversationMemberRepository conversationMemberRepository;
    private MessengerRepository messengerRepository;
    private MessengerStatusRepository messengerStatusRepository;
    private MessageService messageService;

    private final User reader = user(1L, "reader@example.test");
    private final User alice = user(2L, "alice@example.test");
    private final User bob = user(3L, "bob@example.test");

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        conversationMemberRepository = mock(ConversationMemberRepository.class);
        messengerRepository = mock(MessengerRepository.class);
        messengerStatusRepository = mock(MessengerStatusRepository.class);
        messageService = new MessageService(
                userRepository,
                mock(ConversationRepository.class),
                conversationMemberRepository,
                messengerRepository,
                messengerStatusRepository,
                mock(MessageUserStateRepository.class),
                mock(MessageReactionRepository.class),
                mock(ChatRateLimiterService.class),
                mock(ChatObservabilityService.class),
                mock(ChatUploadRepository.class),
                mock(MessageAttachmentRepository.class)
        );
    }

    @Test
    void seenConversationSendsAReceiptToEverySenderInAGroup() {
        when(userRepository.findById(reader.getId())).thenReturn(Optional.of(reader));
        when(conversationMemberRepository.existsByConversationIdAndUserId(CONVERSATION_ID, reader.getId())).thenReturn(true);
        when(messengerStatusRepository.findUnseenByConversationIdAndUserId(CONVERSATION_ID, reader.getId())).thenReturn(List.of(
                unseen(message(10L, alice)),
                unseen(message(11L, bob)),
                unseen(message(12L, alice))
        ));

        SeenConversationResult result = messageService.handleSeenConversation(reader.getId(), new SeenConversationRequest(CONVERSATION_ID));

        Map<String, List<Long>> idsByDestination = result.receipts().stream()
                .collect(Collectors.toMap(SeenConversationReceipt::destination, receipt -> receipt.response().messageIds()));
        assertEquals(Map.of(
                "alice@example.test", List.of(10L, 12L),
                "bob@example.test", List.of(11L)
        ), idsByDestination);
        assertEquals(List.of(10L, 11L, 12L), result.response().messageIds());
    }

    @Test
    void seenConversationWithNothingUnseenSendsNoReceipts() {
        when(userRepository.findById(reader.getId())).thenReturn(Optional.of(reader));
        when(conversationMemberRepository.existsByConversationIdAndUserId(CONVERSATION_ID, reader.getId())).thenReturn(true);
        when(messengerStatusRepository.findUnseenByConversationIdAndUserId(CONVERSATION_ID, reader.getId())).thenReturn(List.of());

        SeenConversationResult result = messageService.handleSeenConversation(reader.getId(), new SeenConversationRequest(CONVERSATION_ID));

        assertTrue(result.receipts().isEmpty());
    }

    @Test
    void recalledMessageCannotBeEdited() {
        Messenger recalled = message(20L, alice);
        recalled.setContent("noi dung goc");
        recalled.setRecalledAt(LocalDateTime.now());
        when(userRepository.findById(alice.getId())).thenReturn(Optional.of(alice));
        when(messengerRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(recalled));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> messageService.handleEditMessage(alice.getId(), new EditMessageRequest(20L, "noi dung moi"))
        );

        assertEquals("Khong the sua tin nhan da thu hoi", exception.getMessage());
        assertEquals("noi dung goc", recalled.getContent());
        verify(messengerRepository, never()).save(any());
    }

    private static User user(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setUserName(email.substring(0, email.indexOf('@')));
        return user;
    }

    private static Messenger message(Long id, User sender) {
        Conversations conversation = new Conversations();
        conversation.setId(CONVERSATION_ID);
        Messenger messenger = new Messenger();
        messenger.setId(id);
        messenger.setUser(sender);
        messenger.setConversation(conversation);
        return messenger;
    }

    private MessengerStatus unseen(Messenger messenger) {
        MessengerStatus status = new MessengerStatus();
        status.setMessenger(messenger);
        status.setUser(reader);
        return status;
    }
}
