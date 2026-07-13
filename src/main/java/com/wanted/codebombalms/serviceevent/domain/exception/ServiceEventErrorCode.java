package com.wanted.codebombalms.serviceevent.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServiceEventErrorCode implements ErrorCode {

    // 브리핑
    BRIEFING_REGENERATE_COOLDOWN("SEC-001", "브리핑 재생성은 분당 1회만 가능합니다."),
    BRIEFING_GENERATION_FAILED("SEC-002", "AI 브리핑 생성에 실패했습니다."),

    // 요약 조회
    INVALID_SUMMARY_PERIOD("SEC-003", "지원하지 않는 기간 파라미터입니다. (today/week/2m)");

    private final String code;
    private final String message;
}
