package com.wanted.codebombalms.global.infrastructure.logging.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private static final Logger businessLog =
            LoggerFactory.getLogger("com.wanted.codebombalms.business");

    private static final Logger performanceLog =
            LoggerFactory.getLogger("com.wanted.codebombalms.performance");

    @Around("@annotation(com.wanted.codebombalms.global.infrastructure.logging.aop.LogBusiness)")
    public Object logBusinessEvent(ProceedingJoinPoint jp) throws Throwable {
        String className = jp.getTarget().getClass().getSimpleName();
        String methodName = jp.getSignature().getName();

        businessLog.info("[{}] {} 시작 - args: {}", className, methodName, jp.getArgs());

        try {
            Object result = jp.proceed();
            businessLog.info("[{}] {} 완료", className, methodName);
            return result;
        } catch (Exception e) {
            businessLog.warn("[{}] {} 실패 - {}", className, methodName, e.getMessage());
            throw e;
        }
    }

    @Around("@annotation(com.wanted.codebombalms.global.infrastructure.logging.aop.LogPerformance)")
    public Object logPerformance(ProceedingJoinPoint jp) throws Throwable {
        long start = System.currentTimeMillis();
        String className = jp.getTarget().getClass().getSimpleName();
        String methodName = jp.getSignature().getName();

        try {
            return jp.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;

            if (duration >= 1000) {
                performanceLog.warn("[SLOW] {}.{} - {}ms", className, methodName, duration);
            } else {
                performanceLog.info("{}.{} - {}ms", className, methodName, duration);
            }
        }
    }
}