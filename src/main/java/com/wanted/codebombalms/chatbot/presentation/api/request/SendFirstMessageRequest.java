package com.wanted.codebombalms.chatbot.presentation.api.request;

public record SendFirstMessageRequest(
        String userMessage,
        Long problemSetId,
        Long problemId
) {}
