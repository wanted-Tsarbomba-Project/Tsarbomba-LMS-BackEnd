package com.wanted.codebombalms.chatbot.application.result;

import java.time.Instant;

public record ChatRoomResult(
        Long roomId,
        Long problemSetId,
        Long problemId,
        String title,
        Instant createdAt,
        Instant updatedAt
) {}
