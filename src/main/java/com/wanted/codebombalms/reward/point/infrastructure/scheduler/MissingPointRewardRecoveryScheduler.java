package com.wanted.codebombalms.reward.point.infrastructure.scheduler;

import com.wanted.codebombalms.reward.point.application.service.MissingPointRewardRecoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MissingPointRewardRecoveryScheduler {

    private final MissingPointRewardRecoveryService recoveryService;

    @Scheduled(
            fixedDelayString = "${reward.point.missing-recovery.fixed-delay-ms:300000}",
            initialDelayString = "${reward.point.missing-recovery.initial-delay-ms:60000}"
    )
    public void recoverMissingTasks() {
        int scheduledCount = recoveryService.recover(100);

        if (scheduledCount > 0) {
            log.info(
                    "event=missing_reward_tasks_recovered scheduledCount={}",
                    scheduledCount
            );
        }
    }
}
