package com.wanted.codebombalms.reward.point.infrastructure.event;

import com.wanted.codebombalms.reward.point.application.port.RecordRewardMetricsPort;
import com.wanted.codebombalms.reward.point.application.usecase.ProcessPointRewardTaskUseCase;
import com.wanted.codebombalms.reward.point.application.usecase.SchedulePointRewardTaskUseCase;
import com.wanted.codebombalms.submission.domain.event.ProblemSolvedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointRewardEventHandler {

    private final SchedulePointRewardTaskUseCase schedulePointRewardTaskUseCase;
    private final ProcessPointRewardTaskUseCase processPointRewardTaskUseCase;
    private final RecordRewardMetricsPort rewardMetrics;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void scheduleAndProcess(ProblemSolvedEvent event) {
        try {
            schedulePointRewardTaskUseCase.schedule(
                    event.userId(),
                    event.problemId(),
                    event.submissionId(),
                    event.point()
            );

            rewardMetrics.recordScheduled();
            rewardMetrics.recordSchedule(RecordRewardMetricsPort.ScheduleResult.SCHEDULED);

            log.info(
                    "event=reward_point_task_scheduled userId={} problemId={} submissionId={} point={}",
                    event.userId(),
                    event.problemId(),
                    event.submissionId(),
                    event.point()
            );
        } catch (DataIntegrityViolationException e) {
            rewardMetrics.recordSchedule(RecordRewardMetricsPort.ScheduleResult.ALREADY_SCHEDULED);

            log.info(
                    "event=reward_point_task_schedule_skipped reason=already_scheduled userId={} problemId={} submissionId={}",
                    event.userId(),
                    event.problemId(),
                    event.submissionId()
            );
            return;
        } catch (Exception e) {
            rewardMetrics.recordSchedule(RecordRewardMetricsPort.ScheduleResult.FAILED);

            log.error(
                    "event=reward_point_task_schedule_failed userId={} problemId={} submissionId={} exceptionType={}",
                    event.userId(),
                    event.problemId(),
                    event.submissionId(),
                    e.getClass().getSimpleName(),
                    e
            );
            return;

        }

        try {
            processPointRewardTaskUseCase.process(event.submissionId());
        } catch (Exception e) {
            log.error(
                    "event=reward_point_task_immediate_process_failed submissionId={} exceptionType={}",
                    event.submissionId(),
                    e.getClass().getSimpleName(),
                    e
            );
        }
    }
}
