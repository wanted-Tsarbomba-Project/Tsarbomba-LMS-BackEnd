package com.wanted.codebombalms.chatbot.infrastructure.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 챗봇 도메인 커스텀 메트릭 (관측 Layer 3).
 *
 * <p>HTTP 메트릭({@code http_server_requests})은 요청 전체(컨트롤러~직렬화) 시간만 안다.
 * 이 Timer 는 "목록 조회 내부 구간"만 분리 측정한다. 부하 중 HTTP p95 ↑ 와 이 timer ↑ 가
 * 함께 움직이면 병목 = 목록 쿼리(N+1) 로 좁힐 수 있다.
 */
@Component
public class ChatMetrics {

    private final Timer listQueryTimer;

    public ChatMetrics(MeterRegistry registry) {
        // 등록명엔 _seconds 를 붙이지 않는다. Timer 라서 Prometheus 가
        // chat_room_list_query_duration_seconds_{count,sum,max} 로 자동 변환한다.
        this.listQueryTimer = Timer.builder("chat_room_list_query_duration")
                .description("채팅방 목록 조회 구간 시간(문제 제목 fanout 포함)")
                .register(registry);
    }

    /** 채팅방 목록 조회(제목 fanout 포함) 구간 소요 시간 기록. */
    public void recordListQuery(long elapsedNanos) {
        listQueryTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }
}
