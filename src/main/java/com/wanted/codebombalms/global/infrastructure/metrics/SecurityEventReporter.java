package com.wanted.codebombalms.global.infrastructure.metrics;

/** ErrorCode(예: AUT-001) 기반 보안 이벤트 기록 포트. 구현체는 도메인 계층(auth)에 위치한다. */
public interface SecurityEventReporter {
    void reportByErrorCode(String errorCode);
}
