package com.wanted.codebombalms.chatbot.infrastructure.metrics;

import com.wanted.codebombalms.chatbot.application.model.AiChatStreamChunk;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 챗봇 도메인 커스텀 메트릭 (관측 Layer 3).
 *
 * <p>HTTP 메트릭({@code http_server_requests})은 요청 전체(컨트롤러~직렬화) 시간만 안다.
 * 이 Timer 는 "목록 조회 내부 구간"만 분리 측정한다. 부하 중 HTTP p95 ↑ 와 이 timer ↑ 가
 * 함께 움직이면 병목 = 목록 쿼리(N+1) 로 좁힐 수 있다.
 *
 * <p>토큰 사용량 Counter 는 입력/출력을 분리 누적한다(단가가 달라 비용 계산 시 분리 필수).
 * total 은 두 Counter 의 합이라 별도 등록하지 않는다.
 */
@Slf4j
@Component
public class ChatMetrics {

    private final Timer listQueryTimer;
    private final Counter promptTokensCounter;
    private final Counter completionTokensCounter;

    public ChatMetrics(MeterRegistry registry) {
        // 등록명엔 _seconds 를 붙이지 않는다. Timer 라서 Prometheus 가
        // chat_room_list_query_duration_seconds_{count,sum,max} 로 자동 변환한다.
        this.listQueryTimer = Timer.builder("chat_room_list_query_duration")
                .description("채팅방 목록 조회 구간 시간(문제 제목 fanout 포함)")
                .register(registry);
        this.promptTokensCounter = Counter.builder("chat_prompt_tokens_total")
                .description("AI 응답 입력(prompt) 토큰 누적량")
                .register(registry);
        this.completionTokensCounter = Counter.builder("chat_completion_tokens_total")
                .description("AI 응답 출력(completion) 토큰 누적량")
                .register(registry);
    }

    /** 채팅방 목록 조회(제목 fanout 포함) 구간 소요 시간 기록. */
    public void recordListQuery(long elapsedNanos) {
        listQueryTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    /**
     * AI 응답 1건의 토큰 사용량을 누적한다. DB 저장 성공 여부와 무관하게,
     * 실제 소비된 토큰(=발생한 비용)을 반영하기 위해 항상 호출된다.
     */
    public void recordUsage(AiChatStreamChunk.TokenUsage usage) {
        if (usage.totalTokens() == 0) {
            log.warn("event=chat_token_usage_empty - FastAPI 가 토큰 사용량을 0 으로 반환함(계약 확인 필요)");
        }
        promptTokensCounter.increment(usage.promptTokens());
        completionTokensCounter.increment(usage.completionTokens());
    }
}
