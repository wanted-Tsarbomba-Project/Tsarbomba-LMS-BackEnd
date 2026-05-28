package com.wanted.codebombalms.chatbot.application.command;

public record CreateChatRoomCommand(
        Long userId,
        Long problemSetId,
        Long problemId
) {}
