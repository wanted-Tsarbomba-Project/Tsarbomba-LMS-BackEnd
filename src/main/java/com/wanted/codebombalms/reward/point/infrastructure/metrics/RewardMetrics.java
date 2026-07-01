package com.wanted.codebombalms.reward.point.infrastructure.metrics;

import com.wanted.codebombalms.reward.point.application.port.RecordRewardMetricsPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RewardMetrics implements RecordRewardMetricsPort {

    private final Counter scheduledCounter;
    private final Map<ProcessResult, Counter> processedCounters;
    private final AtomicLong pendingTasks = new AtomicLong();
    private final Map<ProcessResult, Timer> processTimers;

    public RewardMetrics(MeterRegistry registry) {
        this.scheduledCounter = Counter.builder("reward_point_task_scheduled")
                .description("Committed point reward tasks")
                .register(registry);

        this.processedCounters = new EnumMap<>(ProcessResult.class);
        for (ProcessResult result : ProcessResult.values()) {
            processedCounters.put(
                    result,
                    Counter.builder("reward_point_task_processed")
                            .description("Point reward task processing outcomes")
                            .tag("result", result.tagValue())
                            .register(registry)
            );
        }

        Gauge.builder(
                        "reward_point_task_pending",
                        pendingTasks,
                        AtomicLong::doubleValue
                )
                .description("Current pending point reward task count")
                .register(registry);
        this.processTimers = new EnumMap<>(ProcessResult.class);
        for (ProcessResult result : ProcessResult.values()) {
            processTimers.put(
                    result,
                    Timer.builder("reward_point_task_process_duration")
                            .description("Point reward task processing duration")
                            .tag("result", result.tagValue())
                            .register(registry)
            );
        }
    }

    @Override
    public void recordScheduled() {
        scheduledCounter.increment();
    }

    @Override
    public void recordProcessed(ProcessResult result) {
        processedCounters.get(result).increment();
    }


    @Override
    public void updatePending(long pendingCount) {
        pendingTasks.set(Math.max(pendingCount, 0));
    }

    @Override
    public void recordProcess(ProcessResult result, long elapsedNanos) {
        processTimers.get(result).record(elapsedNanos, TimeUnit.NANOSECONDS);
    }
}
