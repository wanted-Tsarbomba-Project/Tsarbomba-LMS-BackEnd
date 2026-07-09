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
import org.springframework.dao.DataIntegrityViolationException;

import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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
    void schedulesAndProcessesRewardTaskAfterCommit() {
        ProblemSolvedEvent event = new ProblemSolvedEvent(10L, 20L, 30L, 100);

        handler.scheduleAndProcess(event);

        verify(schedulePointRewardTaskUseCase).schedule(10L, 20L, 30L, 100);
        verify(rewardMetrics).recordScheduled();
        verify(rewardMetrics).recordSchedule(RecordRewardMetricsPort.ScheduleResult.SCHEDULED);
        verify(processPointRewardTaskUseCase).process(30L);
    }

    @Test
    void skipsProcessingWhenRewardTaskAlreadyScheduled() {
        ProblemSolvedEvent event = new ProblemSolvedEvent(10L, 20L, 30L, 100);

        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(schedulePointRewardTaskUseCase)
                .schedule(10L, 20L, 30L, 100);

        handler.scheduleAndProcess(event);

        verify(rewardMetrics).recordSchedule(RecordRewardMetricsPort.ScheduleResult.ALREADY_SCHEDULED);
        verify(rewardMetrics, never()).recordScheduled();
        verify(processPointRewardTaskUseCase, never()).process(anyLong());
    }

    @Test
    void recordsFailedMetricWhenRewardTaskScheduleFails() {
        ProblemSolvedEvent event = new ProblemSolvedEvent(10L, 20L, 30L, 100);

        doThrow(new RuntimeException("schedule failed"))
                .when(schedulePointRewardTaskUseCase)
                .schedule(10L, 20L, 30L, 100);

        handler.scheduleAndProcess(event);

        verify(rewardMetrics).recordSchedule(RecordRewardMetricsPort.ScheduleResult.FAILED);
        verify(rewardMetrics, never()).recordScheduled();
        verify(processPointRewardTaskUseCase, never()).process(anyLong());
    }
}
