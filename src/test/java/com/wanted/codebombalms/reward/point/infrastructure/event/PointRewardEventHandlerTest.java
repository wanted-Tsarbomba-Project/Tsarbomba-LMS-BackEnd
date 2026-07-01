package com.wanted.codebombalms.reward.point.infrastructure.event;

import com.wanted.codebombalms.reward.point.application.port.RecordRewardMetricsPort;
import com.wanted.codebombalms.reward.point.application.usecase.ProcessPointRewardTaskUseCase;
import com.wanted.codebombalms.reward.point.application.usecase.SchedulePointRewardTaskUseCase;
import com.wanted.codebombalms.submission.domain.event.ProblemSolvedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PointRewardEventHandlerTest {

    @Mock
    private SchedulePointRewardTaskUseCase schedulePointRewardTaskUseCase;

    @Mock
    private ProcessPointRewardTaskUseCase processPointRewardTaskUseCase;

    @Mock
    private RecordRewardMetricsPort rewardMetrics;

    @InjectMocks
    private PointRewardEventHandler handler;

    @Test
    void recordsScheduledMetricAfterCommittedEvent() {
        ProblemSolvedEvent event = new ProblemSolvedEvent(
                10L,
                20L,
                30L,
                100
        );

        handler.process(event);

        verify(rewardMetrics).recordScheduled();
        verify(processPointRewardTaskUseCase).process(30L);
    }

    @Test
    void schedulesPersistentTaskBeforeCommit() {
        ProblemSolvedEvent event = new ProblemSolvedEvent(
                10L,
                20L,
                30L,
                100
        );

        handler.schedule(event);

        verify(schedulePointRewardTaskUseCase).schedule(
                10L,
                20L,
                30L,
                100
        );
    }
}
