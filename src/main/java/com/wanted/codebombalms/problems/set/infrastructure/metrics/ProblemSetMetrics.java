package com.wanted.codebombalms.problems.set.infrastructure.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Problem Set 도메인의 문제세트 진입 조회 구간을 측정하는 메트릭 컴포넌트.
 */
@Component
public class ProblemSetMetrics {

    private final Timer entryTimer;

    public ProblemSetMetrics(MeterRegistry registry) {
        this.entryTimer = Timer.builder("problem_set_entry_duration")
                .description("문제세트 진입 화면을 구성하는 데 걸린 시간")
                .register(registry);
    }

    public void recordEntry(long elapsedNanos) {
        entryTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }
}
