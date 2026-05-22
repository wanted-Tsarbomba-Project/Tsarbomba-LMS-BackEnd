package com.wanted.codebombalms.chatbot.presentation.api.response;

import com.wanted.codebombalms.chatbot.application.result.ChatMessageResult;
import com.wanted.codebombalms.chatbot.domain.model.MessageRole;

import java.time.Instant;

public record ChatMessageResponse(
        Long messageId,
        MessageRole role,
        String content,
        Instant createdAt
) {
    public static ChatMessageResponse from(ChatMessageResult result) {
        return new ChatMessageResponse(
                result.messageId(),
                result.role(),
                result.content(),
                result.createdAt()
        );
    }
}