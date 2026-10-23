package com.taihoang.social_backend.dto;

public record SeenConversationReceipt(
        String destination,
        SeenConversationResponse response
) {
}
