package com.wanted.codebombalms.chatbot.presentation.api.response;

import com.wanted.codebombalms.chatbot.application.result.RenameChatRoomResult;

import java.time.Instant;

public record RenameChatRoomResponse(
        Long roomId,
        String title,
        Instant updatedAt
) {
    public static RenameChatRoomResponse from(RenameChatRoomResult result) {
        return new RenameChatRoomResponse(result.roomId(), result.title(), result.updatedAt());
    }
}
