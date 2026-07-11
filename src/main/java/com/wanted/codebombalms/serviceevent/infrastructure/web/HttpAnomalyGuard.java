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
 * HTTP 예외 신호(갈래 C)의 분당 적재 상한 가드 (#606).
 *
 * <p>목적: 봇 스캔·장애 폭주 시 service_event INSERT 폭발 방지.
 * 키는 반드시 정규화된 라우트 템플릿 기반이어야 한다(raw URI 금지 — 스캐너가
 * 무한 경로를 만들면 키 카디널리티가 폭발한다). 캐시 자체도 maximumSize 로 바운드.
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

    /** 미인증 401 은 라우트 무관 전역 하드 캡 — 스캔 트래픽은 경로가 산탄이라 라우트 키가 무의미 */
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
