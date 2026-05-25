package com.wanted.codebombalms.global.infrastructure.cleanup;

import com.wanted.codebombalms.global.application.cleanup.HardDeleteExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
// 등록된 모든 하드 딜리트 대상을 동일한 주기로 실행한다.
public class HardDeleteScheduler {

    private final HardDeleteExecutor hardDeleteExecutor;

    // 매일 새벽 3시에 모든 하드 딜리트 대상을 실행한다.
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void runDailyHardDelete() {
        hardDeleteExecutor.executeAll();
    }
}
