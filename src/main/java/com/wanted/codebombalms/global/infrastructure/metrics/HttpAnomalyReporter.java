package com.wanted.codebombalms.global.infrastructure.metrics;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 5xx 상세 기록 포트 (구현은 serviceevent infra).
 * 사용처: GlobalExceptionHandler
 */
public interface HttpAnomalyReporter {

    /**
     * 서버 오류(500·502)를 원인 예외와 함께 기록.
     * 구현은 예외 미전파 필수 (에러 응답 보호).
     */
    void reportServerError(HttpServletRequest request, int httpStatus, Exception cause);
}
