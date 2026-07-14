package com.wanted.codebombalms.chatbot.presentation.api.response;

import com.wanted.codebombalms.chatbot.application.result.ChatMessageResult;
import com.wanted.codebombalms.chatbot.domain.model.FeedbackRating;
import com.wanted.codebombalms.chatbot.domain.model.MessageRole;

import java.time.Instant;

public record ChatMessageResponse(
        Long messageId,
        MessageRole role,
        String content,
        Instant createdAt,
        // AI 메시지에 남긴 내 평가(UP/DOWN). 평가 없거나 USER 메시지면 null.
        FeedbackRating feedback
) {
    public static ChatMessageResponse from(ChatMessageResult result) {
        return new ChatMessageResponse(
                result.messageId(),
                result.role(),
                result.content(),
                result.createdAt(),
                result.feedback()
        );
    }
}