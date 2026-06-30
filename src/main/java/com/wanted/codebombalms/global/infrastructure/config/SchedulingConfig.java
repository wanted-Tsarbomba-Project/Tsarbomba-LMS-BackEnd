package com.wanted.codebombalms.global.infrastructure.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableScheduling
// 애플리케이션 전역 스케줄링과 비동기 실행 기능을 활성화한다.
public class SchedulingConfig {

    /** 추천 생성 배치가 같은 애플리케이션 인스턴스 안에서 동시에 여러 개 실행되지 않도록 단일 스레드로 제한한다. */
    @Bean(name = "recommendationTaskExecutor")
    public Executor recommendationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1);
        executor.setThreadNamePrefix("recommendation-batch-");
        executor.initialize();
        return executor;
    }

    /** 이메일(SMTP) 발송을 로그인 응답 스레드와 분리 — 발송 지연이 응답을 막지 않도록 전용 풀 사용. */
    @Bean(name = "mailTaskExecutor")
    public Executor mailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("mail-async-");
        // 풀+큐 포화 시 호출 스레드에서 직접 실행 — 메일 유실·로그인 실패(TaskRejectedException) 방지
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
