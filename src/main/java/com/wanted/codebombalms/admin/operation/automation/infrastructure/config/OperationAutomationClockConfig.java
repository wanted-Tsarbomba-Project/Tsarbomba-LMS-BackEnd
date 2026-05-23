package com.wanted.codebombalms.admin.operation.automation.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
// 운영 자동 알림에서 사용할 기준 시간대를 제공한다.
public class OperationAutomationClockConfig {

    @Bean
    public Clock operationAutomationClock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }
}
