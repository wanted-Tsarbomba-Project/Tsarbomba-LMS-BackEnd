package com.wanted.codebombalms.chatbot.application.result;

import com.wanted.codebombalms.chatbot.domain.model.MessageRole;

import java.time.Instant;

public record ChatMessageResult(
        Long messageId,
        MessageRole role,
        String content,
        Instant createdAt
) {}