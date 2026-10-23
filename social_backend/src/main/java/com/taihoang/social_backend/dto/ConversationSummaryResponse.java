package com.taihoang.social_backend.dto;

import com.taihoang.social_backend.Entity.Conversations;

import java.time.LocalDateTime;

/**
 * @param peerUserId the other member of a private chat (null for groups). Clients must use it,
 *                   not {@code name}, to find that person: user names are not unique.
 */
public record ConversationSummaryResponse(
        Long id,
        Conversations.type_chat type,
        String name,
        String avatarUrl,
        LastMessageResponse lastMessage,
        long unreadCount,
        LocalDateTime updatedAt,
        Long peerUserId
) {
}
