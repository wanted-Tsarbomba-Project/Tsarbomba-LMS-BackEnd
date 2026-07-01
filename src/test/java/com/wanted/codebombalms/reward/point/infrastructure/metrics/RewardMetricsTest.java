package com.wanted.codebombalms.reward.point.infrastructure.metrics;

import com.wanted.codebombalms.reward.point.application.port.RecordRewardMetricsPort.ProcessResult;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RewardMetricsTest {

    @Test
    void rewardMetricsExposeExpectedNamesAndTags() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        RewardMetrics metrics = new RewardMetrics(registry);

        metrics.recordScheduled();
        for (ProcessResult result : ProcessResult.values()) {
            metrics.recordProcessed(result);
            metrics.recordProcess(
                    result,
                    TimeUnit.MILLISECONDS.toNanos(durationMillis(result))
            );
        }
        metrics.updatePending(7);

        assertThat(registry.get("reward_point_task_scheduled").counter().count())
                .isEqualTo(1.0);
        for (ProcessResult result : ProcessResult.values()) {
            assertThat(registry.get("reward_point_task_processed")
                    .tag("result", result.tagValue())
                    .counter()
                    .count()).isEqualTo(1.0);
            assertThat(registry.get("reward_point_task_process_duration")
                    .tag("result", result.tagValue())
                    .timer()
                    .totalTime(TimeUnit.MILLISECONDS))
                    .isEqualTo((double) durationMillis(result));
        }
        assertThat(registry.get("reward_point_task_pending").gauge().value())
                .isEqualTo(7.0);
    }

    @Test
    void pendingGaugeDoesNotAcceptNegativeValues() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        RewardMetrics metrics = new RewardMetrics(registry);

        metrics.updatePending(-1);

        assertThat(registry.get("reward_point_task_pending").gauge().value())
                .isZero();
    }

    private long durationMillis(ProcessResult result) {
        return 100L + result.ordinal();
    }
}
