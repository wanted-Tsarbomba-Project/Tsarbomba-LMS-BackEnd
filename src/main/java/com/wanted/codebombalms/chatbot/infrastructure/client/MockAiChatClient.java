package com.wanted.codebombalms.chatbot.infrastructure.client;

import com.wanted.codebombalms.chatbot.application.model.AiChatStreamChunk;
import com.wanted.codebombalms.chatbot.application.model.ChatContext;
import com.wanted.codebombalms.chatbot.application.port.AiChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Profile("mock")
public class MockAiChatClient implements AiChatClient {

    private final AtomicInteger callCount = new AtomicInteger(0);

    @Override
    public Flux<AiChatStreamChunk> stream(ChatContext context) {
        int count = callCount.incrementAndGet();
        String answer = "Fastapi 반환 mock입니다." + count + " 토큰 단위로 스트리밍됩니다.";

        Flux<AiChatStreamChunk> tokens = Flux.fromArray(answer.split("(?<=\\s)"))
                .delayElements(Duration.ofMillis(50))
                .map(AiChatStreamChunk.Token::new);

        // 토큰 저장/메트릭을 로컬에서 검증할 수 있도록 0이 아닌 임의 사용량을 방출한다.
        int completion = answer.split("\\s+").length;
        int prompt = 20 + completion;
        Flux<AiChatStreamChunk> done = Flux.just(
                new AiChatStreamChunk.Done(
                        new AiChatStreamChunk.TokenUsage(prompt, completion, prompt + completion))
        );

        return Flux.concat(tokens, done);
    }
}
