package com.wanted.codebombalms.global.infrastructure.metrics;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 5xx 상세 기록 포트 (#607 리뷰 반영).
 * SecurityEventReporter 와 동일한 패턴 — presentation(GlobalExceptionHandler)은
 * 이 인터페이스만 알고, 구현(service_event 적재)은 serviceevent infra 에 둔다.
 */
public interface HttpAnomalyReporter {

    /**
     * 서버 오류(500·502)를 원인 예외와 함께 기록한다.
     * 구현은 절대 예외를 던지지 않아야 한다 (에러 응답 보호).
     */
    void reportServerError(HttpServletRequest request, int httpStatus, Exception cause);
}
