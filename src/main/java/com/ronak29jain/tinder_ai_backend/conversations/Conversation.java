package com.ronak29jain.tinder_ai_backend.conversations;

import java.util.List;

public record Conversation(
        String id,
        List<String> profileIds,
        List<ChatMessage> message
) {
}
