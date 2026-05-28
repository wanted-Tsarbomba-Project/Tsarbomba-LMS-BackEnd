package com.wanted.codebombalms.admin.operation.automation.infrastructure.scheduler;

import com.wanted.codebombalms.admin.operation.automation.application.usecase.RunOperationRuleUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
// 매일 오전 9시에 운영 자동 규칙 실행 유스케이스를 호출한다.
public class OperationRuleScheduler {

    private final RunOperationRuleUseCase runOperationRuleUseCase;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void runDailyOperationRules() {
        runOperationRuleUseCase.run();
    }
}
