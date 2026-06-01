package com.wanted.codebombalms.chatbot.presentation.api.response;

import com.wanted.codebombalms.chatbot.application.result.ChatRoomResult;

import java.time.Instant;

public record ChatRoomResponse(
        Long roomId,
        Long problemSetId,
        Long problemId,
        String title,
        Instant createdAt,
        Instant updatedAt
) {
    public static ChatRoomResponse from(ChatRoomResult result) {
        return new ChatRoomResponse(
                result.roomId(),
                result.problemSetId(),
                result.problemId(),
                result.title(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
