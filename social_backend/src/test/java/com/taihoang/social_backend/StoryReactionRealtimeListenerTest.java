package com.taihoang.social_backend;

import com.taihoang.social_backend.Entity.ReactionType;
import com.taihoang.social_backend.Service.StoryReactionRealtimeListener;
import com.taihoang.social_backend.dto.StoryReactionChangedEvent;
import com.taihoang.social_backend.dto.StoryReactionRealtimePayload;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class StoryReactionRealtimeListenerTest {

    @Test
    void payloadIsRoutedByEmailButDoesNotContainIt() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        StoryReactionRealtimeListener listener = new StoryReactionRealtimeListener(messagingTemplate);
        StoryReactionChangedEvent event = new StoryReactionChangedEvent(
                9L, 3L, "actor@example.test", "Lan", "https://cdn/lan.png",
                "author@example.test", ReactionType.values()[0], "ADD"
        );

        listener.handleStoryReactionChanged(event);

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSendToUser(eq("author@example.test"), eq("/queue/story.reactions"), payload.capture());
        StoryReactionRealtimePayload sent = assertInstanceOf(StoryReactionRealtimePayload.class, payload.getValue());
        assertEquals(9L, sent.storyId());
        assertEquals("ADD", sent.action());
        assertFalse(Arrays.stream(StoryReactionRealtimePayload.class.getRecordComponents())
                .map(RecordComponent::getName)
                .anyMatch(name -> name.toLowerCase().contains("email")));
    }
}
