package com.wanted.codebombalms.submission.infrastructure.metrics;

import com.wanted.codebombalms.submission.application.port.RecordSubmissionMetricsPort;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SubmissionMetrics implements RecordSubmissionMetricsPort {

    private final Timer totalTimer;
    private final Timer prepareTimer;
    private final Timer gradingTimer;
    private final Timer saveTimer;

    public SubmissionMetrics(MeterRegistry registry) {
        this.totalTimer = Timer.builder("submission_total_duration")
                .description("Submission total processing duration")
                .register(registry);

        this.prepareTimer = Timer.builder("submission_prepare_duration")
                .description("Submission validation and lookup duration")
                .register(registry);

        this.gradingTimer = Timer.builder("submission_grading_duration")
                .description("Submission external grading duration")
                .register(registry);

        this.saveTimer = Timer.builder("submission_save_duration")
                .description("Submission and test result save duration")
                .register(registry);
    }
    @Override
    public void recordTotal(long elapsedNanos) {
        totalTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }
    @Override
    public void recordPrepare(long elapsedNanos) {
        prepareTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }
    @Override
    public void recordGrading(long elapsedNanos) {
        gradingTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }
    @Override
    public void recordSave(long elapsedNanos) {
        saveTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }
}
