package com.wanted.codebombalms.chatbot.presentation.api.request;

public record ChatMessageRequest(
        Long problemId,
        String userMessage
) {}