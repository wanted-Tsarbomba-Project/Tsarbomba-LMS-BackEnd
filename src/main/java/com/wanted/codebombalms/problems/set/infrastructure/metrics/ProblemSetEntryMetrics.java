package com.wanted.codebombalms.problems.set.infrastructure.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class ProblemSetEntryMetrics {

    private final Timer totalTimer;
    private final Timer accessTimer;
    private final Timer problemSetTimer;
    private final Timer progressTimer;
    private final Timer progressItemsTimer;
    private final Timer problemDetailsTimer;

    public ProblemSetEntryMetrics(MeterRegistry registry) {
        this.totalTimer = Timer.builder("problem_set_entry_duration")
                .description("Problem set entry total duration")
                .register(registry);
        this.accessTimer = Timer.builder("problem_set_entry_access_duration")
                .description("Problem set entry access validation duration")
                .register(registry);
        this.problemSetTimer = Timer.builder("problem_set_entry_problem_set_duration")
                .description("Problem set base lookup duration during entry")
                .register(registry);
        this.progressTimer = Timer.builder("problem_set_entry_progress_duration")
                .description("Problem set progress lookup or creation duration during entry")
                .register(registry);
        this.progressItemsTimer = Timer.builder("problem_set_entry_progress_items_duration")
                .description("Problem progress item lookup duration during entry")
                .register(registry);
        this.problemDetailsTimer = Timer.builder("problem_set_entry_problem_details_duration")
                .description("Problem detail lookup and response mapping duration during entry")
                .register(registry);
    }

    public void recordTotal(long elapsedNanos) {
        totalTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void recordAccess(long elapsedNanos) {
        accessTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void recordProblemSet(long elapsedNanos) {
        problemSetTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void recordProgress(long elapsedNanos) {
        progressTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void recordProgressItems(long elapsedNanos) {
        progressItemsTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    public void recordProblemDetails(long elapsedNanos) {
        problemDetailsTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }
}
