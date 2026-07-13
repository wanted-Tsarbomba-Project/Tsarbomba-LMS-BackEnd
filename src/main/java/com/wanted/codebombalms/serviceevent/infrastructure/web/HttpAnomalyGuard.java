package com.wanted.codebombalms.serviceevent.infrastructure.web;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * HTTP 예외 신호의 분당 적재 상한 가드 — 키는 정규화 라우트 필수(카디널리티 폭발 방지).
 */
@Component
public class HttpAnomalyGuard {

    private static final String ANONYMOUS_401_KEY = "__anon_401__";
    private static final int MAX_TRACKED_KEYS = 2_000;

    private final Cache<String, AtomicInteger> minuteCounters;
    private final Counter suppressedCounter;
    private final int routeLimitPerMinute;
    private final int anonymous401LimitPerMinute;

    public HttpAnomalyGuard(
            MeterRegistry meterRegistry,
            @Value("${service-event.anomaly.route-limit-per-minute:10}") int routeLimitPerMinute,
            @Value("${service-event.anomaly.anon-401-limit-per-minute:60}") int anonymous401LimitPerMinute) {
        if (routeLimitPerMinute <= 0 || anonymous401LimitPerMinute <= 0) { // 0 이하면 모든 신호 억제 — 기동 시 차단
            throw new IllegalStateException(
                    "service-event.anomaly 분당 상한은 양수여야 합니다: route=%d, anon401=%d"
                            .formatted(routeLimitPerMinute, anonymous401LimitPerMinute));
        }
        this.minuteCounters = Caffeine.newBuilder()
                .maximumSize(MAX_TRACKED_KEYS)
                .expireAfterWrite(Duration.ofMinutes(1))
                .build();
        this.suppressedCounter = Counter.builder("service_event_guard_suppressed")
                .description("분당 상한 초과로 적재 생략된 HTTP 예외 신호 수")
                .register(meterRegistry);
        this.routeLimitPerMinute = routeLimitPerMinute;
        this.anonymous401LimitPerMinute = anonymous401LimitPerMinute;
    }

    /** 라우트 단위 신호 (5xx·403·지연 등) — key 는 "상태클래스:라우트템플릿" 형태 권장 */
    public boolean tryAcquire(String key) {
        return tryAcquire(key, routeLimitPerMinute);
    }

    /** 미인증 401 — 라우트 무관 전역 하드 캡 */
    public boolean tryAcquireAnonymous401() {
        return tryAcquire(ANONYMOUS_401_KEY, anonymous401LimitPerMinute);
    }

    private boolean tryAcquire(String key, int limitPerMinute) {
        AtomicInteger counter = minuteCounters.get(key, k -> new AtomicInteger());
        if (counter.incrementAndGet() <= limitPerMinute) {
            return true;
        }
        suppressedCounter.increment();
        return false;
    }
}
