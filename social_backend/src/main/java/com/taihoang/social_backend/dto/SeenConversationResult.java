package com.taihoang.social_backend.dto;

import java.util.List;

/**
 * @param response every message the recipient just saw, returned to the recipient
 * @param receipts one receipt per sender, listing only that sender's messages;
 *                 a group can have several senders among the newly seen messages
 */
public record SeenConversationResult(
        SeenConversationResponse response,
        List<SeenConversationReceipt> receipts
) {
}
