package com.wanted.codebombalms.chatbot.application.result;

import java.time.Instant;

public record RenameChatRoomResult(
        Long roomId,
        String title,
        Instant updatedAt
) {}
