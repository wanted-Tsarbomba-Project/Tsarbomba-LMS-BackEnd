package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.command.SendMessageCommand;
import com.wanted.codebombalms.chatbot.application.model.AiChatStreamChunk;
import com.wanted.codebombalms.chatbot.application.model.ChatContext;
import com.wanted.codebombalms.chatbot.application.port.AiChatClient;
import com.wanted.codebombalms.chatbot.application.port.RecordTokenUsagePort;
import com.wanted.codebombalms.chatbot.application.usecase.ChatMessageCommandUseCase;
import com.wanted.codebombalms.chatbot.domain.exception.ChatErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageCommandService implements ChatMessageCommandUseCase {

    private final AiChatClient aiChatClient;
    private final ChatMessageTransactionService txService;
    private final RecordTokenUsagePort recordTokenUsagePort;

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
                    if (chunk instanceof AiChatStreamChunk.Done done) {
                        // 메트릭은 저장 성공 여부와 무관하게(=실제 소비된 토큰) 항상 먼저 누적한다.
                        recordTokenUsagePort.recordUsage(done.usage());
                        return Mono.fromRunnable(
                                        () -> txService.saveAiAnswer(
                                                command.roomId(), accumulated.toString(), done.usage()))
                                .subscribeOn(Schedulers.boundedElastic())
                                .thenReturn((AiChatStreamChunk) chunk)
                                // 저장 실패해도 답변은 이미 생성·전송됨 → done 으로 정상 종료(손실은 로그/알림으로)
                                .onErrorResume(e -> {
                                    log.error("event=chat_save_failed roomId={}", command.roomId(), e);
                                    return Mono.just((AiChatStreamChunk) chunk);
                                });
                    }
                    return Mono.just(chunk);
                })
                // 4) 종단 안전망: 어떤 예외든 abort(=ERR_INCOMPLETE_CHUNKED_ENCODING) 대신
                //    error 프레임으로 변환해 스트림을 정상 complete 시킨다.
                .onErrorResume(e -> {
                    log.error("event=chat_stream_aborted roomId={}", command.roomId(), e);
                    return Flux.just(new AiChatStreamChunk.Error(
                            ChatErrorCode.AI_RESPONSE_FAILED.getCode(),
                            ChatErrorCode.AI_RESPONSE_FAILED.getMessage()));
                })
                // 다음 재현 시 종단 신호(onComplete/onError/cancel)로 원인 구간을 즉시 가른다.
                .doFinally(signal ->
                        log.info("event=chat_stream_end roomId={} signal={}", command.roomId(), signal));
    }
}
