package com.ronak29jain.tinder_ai_backend.conversations;

import java.time.LocalDateTime;

public record ChatMessage(
        String message,
        String senderId,
        LocalDateTime messageTime
) {
}
