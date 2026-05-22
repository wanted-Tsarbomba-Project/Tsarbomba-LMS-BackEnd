package com.wanted.codebombalms.chatbot.application.port;

import com.wanted.codebombalms.chatbot.application.model.ChatContext;

public interface AiChatClient {

    // ChatContext를 FastAPI에 전송하고 AI 응답 반환
    AiChatClientResponse call(ChatContext context);

    record AiChatClientResponse(
            String answer,
            boolean isAnswerDetected,
            int retryCount,
            TokenUsage tokenUsage
    ) {}

    record TokenUsage(
            int promptTokens,
            int completionTokens,
            int totalTokens
    ) {}
}