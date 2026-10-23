package com.taihoang.social_backend.dto;

import com.taihoang.social_backend.Entity.ReactionType;

/**
 * What the story author's client receives on /queue/story.reactions.
 * Unlike {@link StoryReactionChangedEvent}, it carries no email addresses: those are only
 * needed server-side to route the message.
 */
public record StoryReactionRealtimePayload(
        Long storyId,
        Long actorId,
        String actorName,
        String actorAvatarUrl,
        ReactionType type,
        String action
) {
    public static StoryReactionRealtimePayload from(StoryReactionChangedEvent event) {
        return new StoryReactionRealtimePayload(
                event.storyId(),
                event.actorId(),
                event.actorName(),
                event.actorAvatarUrl(),
                event.type(),
                event.action()
        );
    }
}
