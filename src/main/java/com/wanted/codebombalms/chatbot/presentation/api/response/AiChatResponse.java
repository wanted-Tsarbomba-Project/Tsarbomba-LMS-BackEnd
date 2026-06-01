package com.wanted.codebombalms.chatbot.presentation.api.response;

import com.wanted.codebombalms.chatbot.application.result.AiChatResult;

public record AiChatResponse(
        String answer
) {
    public static AiChatResponse from(AiChatResult result) {
        return new AiChatResponse(result.answer());
    }
}