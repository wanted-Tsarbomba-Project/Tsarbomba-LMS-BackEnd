package com.wanted.codebombalms.reward.point.application.service;

import com.wanted.codebombalms.reward.point.application.port.FindMissingPointRewardTargetsPort;
import com.wanted.codebombalms.reward.point.application.port.RecordRewardMetricsPort;
import com.wanted.codebombalms.reward.point.application.port.RecordRewardMetricsPort.MissingRecoveryResult;
import com.wanted.codebombalms.reward.point.application.usecase.SchedulePointRewardTaskUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MissingPointRewardRecoveryService {

    private final FindMissingPointRewardTargetsPort findMissingTargetsPort;
    private final SchedulePointRewardTaskUseCase schedulePointRewardTaskUseCase;
    private final RecordRewardMetricsPort rewardMetrics;

    public int recover(int limit) {
        List<FindMissingPointRewardTargetsPort.MissingPointRewardTarget> targets =
                findMissingTargetsPort.findTargets(limit);

        int scheduledCount = 0;
        int targetCount = targets.size();
        int skippedCount = 0;
        int failedCount = 0;

        for (FindMissingPointRewardTargetsPort.MissingPointRewardTarget target : targets) {
            try {
                schedulePointRewardTaskUseCase.schedule(
                        target.userId(),
                        target.problemId(),
                        target.submissionId(),
                        target.point()
                );
                scheduledCount++;
                rewardMetrics.recordMissingRecovery(
                        MissingRecoveryResult.SCHEDULED
                );
            } catch (DataIntegrityViolationException e) {
                skippedCount++;
                rewardMetrics.recordMissingRecovery(
                        MissingRecoveryResult.SKIPPED
                );
                log.info(
                        "event=missing_reward_task_schedule_skipped reason=already_scheduled userId={} problemId={} submissionId={}",
                        target.userId(),
                        target.problemId(),
                        target.submissionId()
                );
            } catch (Exception e) {
                failedCount++;
                rewardMetrics.recordMissingRecovery(
                        MissingRecoveryResult.FAILED
                );
                log.error(
                        "event=missing_reward_task_schedule_failed userId={} problemId={} submissionId={} exceptionType={}",
                        target.userId(),
                        target.problemId(),
                        target.submissionId(),
                        e.getClass().getSimpleName(),
                        e
                );
            }
        }
        log.info(
                "event=missing_reward_recovery_completed targetCount={} scheduledCount={} skippedCount={} failedCount={}",
                targetCount,
                scheduledCount,
                skippedCount,
                failedCount
        );
        return scheduledCount;
    }
}
