package com.wanted.codebombalms.learning.infrastructure.metrics;

import com.wanted.codebombalms.learning.application.port.LearningProgressMetricsPort;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class LearningMetrics implements LearningProgressMetricsPort {

    private final MeterRegistry registry;
    private final Timer studentProgressQueryTimer;
    private final Timer studentProgressItemTimer;
    private final Map<String, Timer> studentProgressSectionTimers = new ConcurrentHashMap<>();

    public LearningMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.studentProgressQueryTimer = Timer.builder("learning_student_progress_query_duration")
                .description("강좌별 학생 학습률 목록 조회 구간 시간")
                .register(registry);
        this.studentProgressItemTimer = Timer.builder("learning_student_progress_item_duration")
                .description("학생 1명 학습률 계산 구간 시간")
                .register(registry);
    }

    @Override
    public void recordStudentProgressQuery(long elapsedNanos) {
        studentProgressQueryTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public void recordStudentProgressItem(long elapsedNanos) {
        studentProgressItemTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public void recordStudentProgressSection(String section, long elapsedNanos) {
        studentProgressSectionTimers.computeIfAbsent(section, this::createStudentProgressSectionTimer)
                .record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    private Timer createStudentProgressSectionTimer(String section) {
        return Timer.builder("learning_student_progress_section_duration")
                .description("Student progress query section duration")
                .tag("section", section)
                .register(registry);
    }
}

// 쿼리 서비스가 시간 재면 여기서 시간을 매트릭으로 저장
