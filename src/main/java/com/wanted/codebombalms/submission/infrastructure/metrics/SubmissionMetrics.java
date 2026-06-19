package com.wanted.codebombalms.submission.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Submission 도메인의 제출/채점 병목 후보 구간을 측정하는 메트릭 컴포넌트.
 */
@Component
public class SubmissionMetrics {

    private final Timer gradingTimer;
    private final Counter failureCounter;

    public SubmissionMetrics(MeterRegistry registry) {
        this.gradingTimer = Timer.builder("submission_grading_duration")
                .description("코드 제출 후 테스트케이스 채점과 제출 결과 저장까지 걸린 시간")
                .register(registry);

        this.failureCounter = Counter.builder("submission_failed_total")
                .description("코드 제출 및 채점 실패 횟수")
                .register(registry);
    }

    public void recordGrading(long elapsedNanos) {
        gradingTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void incrementFailure() {
        failureCounter.increment();
    }
}
