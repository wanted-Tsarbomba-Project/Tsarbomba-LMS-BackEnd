package com.wanted.codebombalms.chatbot.application.command;

public record SendMessageCommand(
        Long userId,
        Long roomId,
        Long problemId,
        String userMessage
) {}