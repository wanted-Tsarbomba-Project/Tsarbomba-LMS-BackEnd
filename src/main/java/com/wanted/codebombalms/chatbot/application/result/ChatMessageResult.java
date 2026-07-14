package com.wanted.codebombalms.chatbot.application.result;

import com.wanted.codebombalms.chatbot.domain.model.FeedbackRating;
import com.wanted.codebombalms.chatbot.domain.model.MessageRole;

import java.time.Instant;

public record ChatMessageResult(
        Long messageId,
        MessageRole role,
        String content,
        Instant createdAt,
        // AI 메시지에 남긴 내 평가(UP/DOWN). 평가 없거나 USER 메시지면 null.
        FeedbackRating feedback
) {}
