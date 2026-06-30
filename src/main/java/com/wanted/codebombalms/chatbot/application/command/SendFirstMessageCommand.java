package com.wanted.codebombalms.chatbot.application.command;

public record SendFirstMessageCommand(
        Long userId,
        String userMessage,
        Long problemSetId,
        Long problemId,
        String traceId
) {}
