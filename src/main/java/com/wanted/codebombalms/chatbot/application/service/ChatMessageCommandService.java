package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.command.SendMessageCommand;
import com.wanted.codebombalms.chatbot.application.model.AiChatStreamChunk;
import com.wanted.codebombalms.chatbot.application.model.ChatContext;
import com.wanted.codebombalms.chatbot.application.port.AiChatClient;
import com.wanted.codebombalms.chatbot.application.usecase.ChatMessageCommandUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class ChatMessageCommandService implements ChatMessageCommandUseCase {

    private final AiChatClient aiChatClient;
    private final ChatMessageTransactionService txService;

    @Override
    public Flux<AiChatStreamChunk> send(SendMessageCommand command) {
        // 1) 스트림 전 동기 구간(블로킹 tx) — 호출 스레드에서 먼저 끝낸다
        ChatContext context = txService.prepare(command);

        // 2) 스트리밍: 토큰을 누적하며 그대로 흘린다
        StringBuilder accumulated = new StringBuilder();

        return aiChatClient.stream(context)
                .doOnNext(chunk -> {
                    if (chunk instanceof AiChatStreamChunk.Token token) {
                        accumulated.append(token.text());
                    }
                })
                .concatMap(chunk -> {
                    // 3) Done 수신 = 정상 완료 → 완성된 답만 저장(부분답·취소는 저장 안 됨)
                    if (chunk instanceof AiChatStreamChunk.Done) {
                        return Mono.fromRunnable(
                                        () -> txService.saveAiAnswer(command.roomId(), accumulated.toString()))
                                .subscribeOn(Schedulers.boundedElastic())
                                .thenReturn((AiChatStreamChunk) chunk);
                    }
                    return Mono.just(chunk);
                });
    }
}
