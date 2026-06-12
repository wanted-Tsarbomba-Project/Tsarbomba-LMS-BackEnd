package com.wanted.codebombalms.reward.point.infrastructure.scheduler;

import com.wanted.codebombalms.reward.point.application.usecase.RecoverPendingPointRewardTasksUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointRewardRecoveryScheduler {

    private final RecoverPendingPointRewardTasksUseCase recoverUseCase;

    @Scheduled(
            fixedDelayString = "${reward.point.recovery.fixed-delay-ms:60000}",
            initialDelayString = "${reward.point.recovery.initial-delay-ms:60000}"
    )
    public void recoverPendingTasks() {
        int processedCount = recoverUseCase.recover();

        if (processedCount > 0) {
            log.info(
                    "포인트 지급 작업 재처리를 완료했습니다. processedCount={}",
                    processedCount
            );
        }
    }
}
