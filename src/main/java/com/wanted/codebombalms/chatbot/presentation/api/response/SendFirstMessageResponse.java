package com.wanted.codebombalms.chatbot.presentation.api.response;

import com.wanted.codebombalms.chatbot.application.result.SendFirstMessageResult;

public record SendFirstMessageResponse(
        Long roomId,
        String answer
) {
    public static SendFirstMessageResponse from(SendFirstMessageResult result) {
        return new SendFirstMessageResponse(result.roomId(), result.answer());
    }
}
