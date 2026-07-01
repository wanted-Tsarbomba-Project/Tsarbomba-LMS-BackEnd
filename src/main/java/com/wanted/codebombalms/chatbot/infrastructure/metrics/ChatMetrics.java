package com.wanted.codebombalms.chatbot.infrastructure.metrics;

import com.wanted.codebombalms.chatbot.application.model.AiChatStreamChunk;
import com.wanted.codebombalms.chatbot.application.port.ChatStreamMetricsPort;
import com.wanted.codebombalms.chatbot.application.port.RecordTokenUsagePort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 챗봇 도메인 커스텀 메트릭 (관측 Layer 3).
 *
 * <p>HTTP 메트릭({@code http_server_requests})은 요청 전체(컨트롤러~직렬화) 시간만 안다.
 * 이 Timer 는 "목록 조회 내부 구간"만 분리 측정한다. 부하 중 HTTP p95 ↑ 와 이 timer ↑ 가
 * 함께 움직이면 병목 = 목록 쿼리(N+1) 로 좁힐 수 있다.
 *
 * <p>토큰 사용량 Counter 는 입력/출력을 분리 누적한다(단가가 달라 비용 계산 시 분리 필수).
 * total 은 두 Counter 의 합이라 별도 등록하지 않는다.
 *
 * <p>스트림 생명주기 메트릭({@link ChatStreamMetricsPort}) — 운영 대시보드의 "외부 AI 의존"·
 * "스트리밍 건강성" 행을 채운다. AI Timer 두 개는 외부연동 p95 가 핵심 SLI 라
 * {@code publishPercentileHistogram()} 으로 히스토그램 버킷을 노출한다(PromQL {@code histogram_quantile}).
 * outcome/signal 은 동적 태그라 {@link MeterRegistry} 에서 그때그때 빌드한다(같은 name+tag 는 캐시됨).
 */
@Slf4j
@Component
public class ChatMetrics implements RecordTokenUsagePort, ChatStreamMetricsPort {

    private final MeterRegistry registry;
    private final Timer listQueryTimer;
    private final Counter promptTokensCounter;
    private final Counter completionTokensCounter;
    private final Timer timeToFirstTokenTimer;
    private final AtomicInteger activeStreams = new AtomicInteger();

    public ChatMetrics(MeterRegistry registry) {
        this.registry = registry;
        // 등록명엔 _seconds 를 붙이지 않는다. Timer 라서 Prometheus 가
        // chat_room_list_query_duration_seconds_{count,sum,max} 로 자동 변환한다.
        this.listQueryTimer = Timer.builder("chat_room_list_query_duration")
                .description("채팅방 목록 조회 구간 시간(문제 제목 fanout 포함)")
                .register(registry);
        // Prometheus 가 counter 에 _total 을 자동으로 붙이므로 등록명에는 붙이지 않는다
        // (chat_prompt_tokens → chat_prompt_tokens_total 로 노출됨).
        this.promptTokensCounter = Counter.builder("chat_prompt_tokens")
                .description("AI 응답 입력(prompt) 토큰 누적량")
                .register(registry);
        this.completionTokensCounter = Counter.builder("chat_completion_tokens")
                .description("AI 응답 출력(completion) 토큰 누적량")
                .register(registry);
        this.timeToFirstTokenTimer = Timer.builder("chat_ai_time_to_first_token")
                .description("스트림 구독부터 첫 토큰 수신까지(TTFT, 사용자 체감 지연)")
                .publishPercentileHistogram()
                .register(registry);
        // 현재 동시 SSE 스트림 수. 단위 suffix 없이 chat_active_streams 로 노출.
        Gauge.builder("chat_active_streams", activeStreams, AtomicInteger::get)
                .description("현재 동시 AI 스트림 수")
                .register(registry);
    }

    /** 채팅방 목록 조회(제목 fanout 포함) 구간 소요 시간 기록. */
    public void recordListQuery(long elapsedNanos) {
        listQueryTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public void onStreamStart() {
        activeStreams.incrementAndGet();
    }

    @Override
    public void onFirstToken(long ttftNanos) {
        timeToFirstTokenTimer.record(ttftNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public void onStreamEnd(long durationNanos, String outcome, String signal) {
        activeStreams.decrementAndGet();
        Timer.builder("chat_ai_stream_duration")
                .description("AI 스트림 전체 소요시간(outcome 별)")
                .tag("outcome", outcome)
                .publishPercentileHistogram()
                .register(registry)
                .record(durationNanos, TimeUnit.NANOSECONDS);
        Counter.builder("chat_stream_terminations")
                .description("스트림 종료 신호 분포(정상완료 vs 중도이탈)")
                .tag("signal", signal)
                .register(registry)
                .increment();
    }

    /**
     * AI 응답 1건의 토큰 사용량을 누적한다. DB 저장 성공 여부와 무관하게,
     * 실제 소비된 토큰(=발생한 비용)을 반영하기 위해 항상 호출된다.
     */
    @Override
    public void recordUsage(AiChatStreamChunk.TokenUsage usage) {
        if (usage.totalTokens() == 0) {
            log.warn("event=chat_token_usage_empty - FastAPI 가 토큰 사용량을 0 으로 반환함(계약 확인 필요)");
        }
        promptTokensCounter.increment(usage.promptTokens());
        completionTokensCounter.increment(usage.completionTokens());
    }
}
