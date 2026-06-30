package com.wanted.codebombalms.reward.point.infrastructure.metrics;

import com.wanted.codebombalms.reward.point.application.port.RecordRewardMetricsPort;
import com.wanted.codebombalms.reward.point.domain.model.PointRewardTaskStatus;
import com.wanted.codebombalms.reward.point.domain.repository.PointRewardTaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RewardPendingMetricsSchedulerTest {

    @Mock
    private PointRewardTaskRepository pointRewardTaskRepository;

    @Mock
    private RecordRewardMetricsPort rewardMetrics;

    @InjectMocks
    private RewardPendingMetricsScheduler scheduler;

    @Test
    void refreshesPendingGaugeFromPersistentTasks() {
        given(pointRewardTaskRepository.countByStatus(PointRewardTaskStatus.PENDING))
                .willReturn(12L);

        scheduler.refreshPendingCount();

        verify(rewardMetrics).updatePending(12L);
    }
}
