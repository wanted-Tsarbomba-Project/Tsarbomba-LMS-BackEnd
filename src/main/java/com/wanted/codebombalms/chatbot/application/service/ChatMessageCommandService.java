package com.wanted.codebombalms.chatbot.application.service;

import com.wanted.codebombalms.chatbot.application.command.SendMessageCommand;
import com.wanted.codebombalms.chatbot.application.model.AiChatStreamChunk;
import com.wanted.codebombalms.chatbot.application.model.ChatContext;
import com.wanted.codebombalms.chatbot.application.port.AiChatClient;
import com.wanted.codebombalms.chatbot.application.port.ChatStreamMetricsPort;
import com.wanted.codebombalms.chatbot.application.port.RecordTokenUsagePort;
import com.wanted.codebombalms.chatbot.application.usecase.ChatMessageCommandUseCase;
import com.wanted.codebombalms.chatbot.domain.exception.ChatErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageCommandService implements ChatMessageCommandUseCase {

    private final AiChatClient aiChatClient;
    private final ChatMessageTransactionService txService;
    private final RecordTokenUsagePort recordTokenUsagePort;
    private final ChatStreamMetricsPort streamMetricsPort;

    @Override
    public Flux<AiChatStreamChunk> send(SendMessageCommand command) {
        // 1) 스트림 전 동기 구간(블로킹 tx) — 호출 스레드에서 먼저 끝낸다
        ChatContext context = txService.prepare(command);

        // 2) per-subscription 상태 — reactive 콜백이 별도 스레드라 MDC 대신 클로저로 들고 다닌다.
        StringBuilder accumulated = new StringBuilder();
        long startNanos = System.nanoTime();
        AtomicBoolean firstToken = new AtomicBoolean(false);
        AtomicReference<String> outcome = new AtomicReference<>("success");
        streamMetricsPort.onStreamStart();

        return aiChatClient.stream(context, command.traceId())
                .doOnNext(chunk -> {
                    if (chunk instanceof AiChatStreamChunk.Token token) {
                        if (firstToken.compareAndSet(false, true)) {
                            streamMetricsPort.onFirstToken(System.nanoTime() - startNanos);
                        }
                        accumulated.append(token.text());
                    } else if (chunk instanceof AiChatStreamChunk.Error err) {
                        // FastAPI 가 error 프레임을 보낸 경우(client onErrorResume 포함) — outcome 갈음 + 추적 로그
                        outcome.set("error");
                        log.error("event=chat_ai_error_chunk userId={} roomId={} code={} traceId={}",
                                command.userId(), command.roomId(), err.code(), command.traceId());
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
                                    log.error("event=chat_save_failed userId={} roomId={} traceId={}",
                                            command.userId(), command.roomId(), command.traceId(), e);
                                    return Mono.just((AiChatStreamChunk) chunk);
                                });
                    }
                    return Mono.just(chunk);
                })
                // 4) 종단 안전망: 어떤 예외든 abort(=ERR_INCOMPLETE_CHUNKED_ENCODING) 대신
                //    error 프레임으로 변환해 스트림을 정상 complete 시킨다.
                .onErrorResume(e -> {
                    outcome.set("error");
                    log.error("event=chat_stream_aborted userId={} roomId={} exceptionType={} traceId={}",
                            command.userId(), command.roomId(), e.getClass().getSimpleName(), command.traceId(), e);
                    return Flux.just(new AiChatStreamChunk.Error(
                            ChatErrorCode.AI_RESPONSE_FAILED.getCode(),
                            ChatErrorCode.AI_RESPONSE_FAILED.getMessage()));
                })
                // 5) 종단: 활성 스트림 -1, 전체 소요/종료신호 기록 + 추적 기준선 로그(정상도 1줄).
                //    durationMs 는 async 라 HTTP 메트릭보다 이 값이 정확하다.
                .doFinally(signal -> {
                    long elapsedNanos = System.nanoTime() - startNanos;
                    streamMetricsPort.onStreamEnd(elapsedNanos, outcome.get(), signal.toString());
                    log.info("event=chat_stream_end userId={} roomId={} signal={} outcome={} durationMs={} userMessageLength={} traceId={}",
                            command.userId(), command.roomId(), signal, outcome.get(),
                            elapsedNanos / 1_000_000,
                            command.userMessage() == null ? 0 : command.userMessage().length(),
                            command.traceId());
                });
    }
}
