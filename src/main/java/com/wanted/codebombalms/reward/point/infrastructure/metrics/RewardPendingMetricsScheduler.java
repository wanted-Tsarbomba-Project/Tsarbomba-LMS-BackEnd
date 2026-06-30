package com.wanted.codebombalms.reward.point.infrastructure.metrics;

import com.wanted.codebombalms.reward.point.application.port.RecordRewardMetricsPort;
import com.wanted.codebombalms.reward.point.domain.model.PointRewardTaskStatus;
import com.wanted.codebombalms.reward.point.domain.repository.PointRewardTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RewardPendingMetricsScheduler {

    private final PointRewardTaskRepository pointRewardTaskRepository;
    private final RecordRewardMetricsPort rewardMetrics;

    @Scheduled(
            fixedDelayString = "${reward.point.metrics.pending-refresh-ms:30000}",
            initialDelayString = "${reward.point.metrics.pending-initial-delay-ms:0}"
    )
    public void refreshPendingCount() {
        try {
            long pendingCount = pointRewardTaskRepository.countByStatus(
                    PointRewardTaskStatus.PENDING
            );
            rewardMetrics.updatePending(pendingCount);
        } catch (Exception e) {
            log.warn(
                    "event=reward_point_pending_metric_refresh_failed exceptionType={}",
                    e.getClass().getSimpleName(),
                    e
            );
        }
    }
}
