package com.taihoang.social_backend.dto;

import com.taihoang.social_backend.Entity.CallType;
import jakarta.validation.constraints.NotNull;

public record CallInviteRequest(
        @NotNull Long conversationId,
        @NotNull Long calleeId,
        @NotNull CallType callType
) {
}
