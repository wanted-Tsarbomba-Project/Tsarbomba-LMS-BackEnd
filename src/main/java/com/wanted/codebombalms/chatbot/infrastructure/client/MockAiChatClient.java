package com.wanted.codebombalms.chatbot.infrastructure.client;

import com.wanted.codebombalms.chatbot.application.model.ChatContext;
import com.wanted.codebombalms.chatbot.application.port.AiChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Profile("mock")
public class MockAiChatClient implements AiChatClient {

    private final AtomicInteger callCount = new AtomicInteger(0);

    @Override
    public AiChatClientResponse call(ChatContext context) {
        int count = callCount.incrementAndGet();
        String answer = "Fastapi 반환 mock입니다." + count;

        return new AiChatClientResponse(
                answer,
                true,
                30,
                new TokenUsage(0, 0, 0)
        );
    }
}