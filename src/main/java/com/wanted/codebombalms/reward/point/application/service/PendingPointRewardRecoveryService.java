package com.wanted.codebombalms.reward.point.application.service;

import com.wanted.codebombalms.reward.point.application.usecase.ProcessPointRewardTaskUseCase;
import com.wanted.codebombalms.reward.point.application.usecase.RecoverPendingPointRewardTasksUseCase;
import com.wanted.codebombalms.reward.point.domain.repository.PointRewardTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PendingPointRewardRecoveryService
        implements RecoverPendingPointRewardTasksUseCase {

    private static final int RECOVERY_BATCH_SIZE = 100;

    private final PointRewardTaskRepository pointRewardTaskRepository;
    private final ProcessPointRewardTaskUseCase processPointRewardTaskUseCase;
    private final Clock clock;

    @Override
    public int recover() {
        var tasks = pointRewardTaskRepository.findRecoverableTasks(
                LocalDateTime.now(clock),
                RECOVERY_BATCH_SIZE
        );

        int attemptedCount = 0;

        for (var task : tasks) {
            try {
                processPointRewardTaskUseCase.process(task.submissionId());
                attemptedCount++;
            } catch (Exception e) {
                log.error(
                        "포인트 지급 작업 재처리 호출에 실패했습니다. submissionId={}",
                        task.submissionId(),
                        e
                );
            }
        }

        return attemptedCount;
    }
}
