package com.taihoang.social_backend.dto;

import com.taihoang.social_backend.Entity.CallStatus;
import com.taihoang.social_backend.Entity.CallType;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record CallEvent(
        String type,
        String callId,
        Long conversationId,
        Long callerId,
        String callerName,
        Long calleeId,
        String calleeName,
        CallType callType,
        CallStatus status,
        Long actorId,
        JsonNode payload,
        LocalDateTime createdAt
) {
}
