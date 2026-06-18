package com.wanted.codebombalms.learning.infrastructure.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class LearningMetrics {

    private final Timer studentProgressQueryTimer;

    public LearningMetrics(MeterRegistry registry) {
        this.studentProgressQueryTimer = Timer.builder("learning_student_progress_query_duration")
                .description("강좌별 학생 학습률 목록 조회 구간 시간")
                .register(registry);
    }

    public void recordStudentProgressQuery(long elapsedNanos) {
        studentProgressQueryTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }
}

// 쿼리 서비스가 시간 재면 여기서 시간을 매트릭으로 저장
