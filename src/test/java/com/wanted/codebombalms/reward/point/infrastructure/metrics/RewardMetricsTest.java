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
        metrics.recordProcessed(ProcessResult.COMPLETED);
        metrics.recordProcessed(ProcessResult.RETRY);
        metrics.recordProcessed(ProcessResult.FAILED);
        metrics.recordProcess(TimeUnit.MILLISECONDS.toNanos(250));
        metrics.updatePending(7);

        assertThat(registry.get("reward_point_task_scheduled").counter().count())
                .isEqualTo(1.0);
        assertThat(registry.get("reward_point_task_processed")
                .tag("result", "completed")
                .counter()
                .count()).isEqualTo(1.0);
        assertThat(registry.get("reward_point_task_processed")
                .tag("result", "retry")
                .counter()
                .count()).isEqualTo(1.0);
        assertThat(registry.get("reward_point_task_processed")
                .tag("result", "failed")
                .counter()
                .count()).isEqualTo(1.0);
        assertThat(registry.get("reward_point_task_process_duration")
                .timer()
                .totalTime(TimeUnit.MILLISECONDS)).isEqualTo(250.0);
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
}
