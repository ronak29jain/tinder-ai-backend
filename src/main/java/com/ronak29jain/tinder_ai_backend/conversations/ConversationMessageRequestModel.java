package com.ronak29jain.tinder_ai_backend.conversations;

public record ConversationMessageRequestModel(
        String message,
        String senderId
) {
}
