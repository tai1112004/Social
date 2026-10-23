package com.taihoang.social_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EditMessageRequest(

        @NotNull(message = "messageId không được để trống")
        Long messageId,

        @NotBlank(message = "content không được để trống")
        @Size(
                max = 5000,
                message = "content không được vượt quá 5000 ký tự"
        )
        String content

) {
}
