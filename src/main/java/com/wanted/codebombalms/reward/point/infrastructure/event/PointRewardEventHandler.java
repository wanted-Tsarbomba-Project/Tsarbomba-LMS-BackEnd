package com.wanted.codebombalms.reward.point.infrastructure.event;

import com.wanted.codebombalms.reward.point.application.port.RecordRewardMetricsPort;
import com.wanted.codebombalms.reward.point.application.usecase.ProcessPointRewardTaskUseCase;
import com.wanted.codebombalms.reward.point.application.usecase.SchedulePointRewardTaskUseCase;
import com.wanted.codebombalms.submission.domain.event.ProblemSolvedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void schedule(ProblemSolvedEvent event) {
        schedulePointRewardTaskUseCase.schedule(
                event.userId(),
                event.problemId(),
                event.submissionId(),
                event.point()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void process(ProblemSolvedEvent event) {
        rewardMetrics.recordScheduled();
        log.info(
                "event=reward_point_task_scheduled userId={} problemId={} submissionId={} point={}",
                event.userId(),
                event.problemId(),
                event.submissionId(),
                event.point()
        );

        try {
            processPointRewardTaskUseCase.process(event.submissionId());
        } catch (Exception e) {
            log.error(
                    "포인트 지급 작업 즉시 처리 실패. submissionId={}",
                    event.submissionId(),
                    e
            );
        }
    }
}
