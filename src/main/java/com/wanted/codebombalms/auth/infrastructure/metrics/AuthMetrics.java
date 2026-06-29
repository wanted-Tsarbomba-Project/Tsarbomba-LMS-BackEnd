package com.wanted.codebombalms.auth.infrastructure.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Auth 도메인 커스텀 메트릭 (관측 Layer 3).
 *
 * <p>HTTP 메트릭({@code http_server_requests})은 요청 전체(컨트롤러~직렬화) 시간만 안다.
 * 이 Timer 는 "로그인 이력 조회 내부 구간(login_history 페이지 쿼리)"만 분리 측정한다.
 * 부하 중 HTTP p95 ↑ 와 이 timer ↑ 가 함께 움직이면 병목 = 이력 조회 쿼리(인덱스
 * 부재 풀스캔/filesort)로 좁힐 수 있다. 인덱스 반영 후 이 timer 가 떨어지면 효과를 증명한다.
 */
@Component
public class AuthMetrics {

    private final Timer loginHistoryQueryTimer;

    public AuthMetrics(MeterRegistry registry) {
        // 등록명엔 _seconds 를 붙이지 않는다. Timer 라서 Prometheus 가
        // auth_login_history_query_duration_seconds_{count,sum,max} 로 자동 변환한다.
        this.loginHistoryQueryTimer = Timer.builder("auth_login_history_query_duration")
                .description("로그인 이력 조회 구간 시간(login_history 페이지 쿼리)")
                .register(registry);
    }

    /** 로그인 이력 조회(login_history 페이지) 구간 소요 시간 기록. */
    public void recordLoginHistoryQuery(long elapsedNanos) {
        loginHistoryQueryTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }
}
