package com.taihoang.social_backend.dto;

import com.taihoang.social_backend.Entity.CallStatus;
import com.taihoang.social_backend.Entity.CallType;

import java.time.LocalDateTime;

public record CallHistoryResponse(
        Long id,
        String callId,
        Long conversationId,
        Long otherUserId,
        String otherUserName,
        String otherUserAvatarUrl,
        CallType callType,
        CallStatus status,
        LocalDateTime createdAt,
        LocalDateTime answeredAt,
        LocalDateTime endedAt,
        Long durationSeconds,
        boolean outgoing
) {
}
