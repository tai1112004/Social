package com.taihoang.social_backend.dto;

import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.JsonNode;

public record CallSignalRequest(
        @NotBlank String callId,
        JsonNode payload
) {
}
