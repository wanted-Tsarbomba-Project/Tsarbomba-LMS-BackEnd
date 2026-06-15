package com.wanted.codebombalms.global.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
// 애플리케이션 전역 스케줄링과 비동기 실행 기능을 활성화한다.
public class SchedulingConfig {
}
