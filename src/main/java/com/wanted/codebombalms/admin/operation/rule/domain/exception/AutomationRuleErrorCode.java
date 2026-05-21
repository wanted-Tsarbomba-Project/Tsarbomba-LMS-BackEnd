package com.wanted.codebombalms.admin.operation.rule.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AutomationRuleErrorCode implements ErrorCode {

    // 중복
    DUPLICATED_RULE_CODE("ADM-ARL-004", "이미 등록된 자동화 규칙입니다."),

    // 입력값 검증
    INVALID_CREATE_REQUEST("ADM-ARL-001", "자동화 규칙 등록 요청이 올바르지 않습니다."),
    INVALID_THRESHOLD_VALUE("ADM-ARL-002", "임계값이 올바르지 않습니다."),
    INVALID_MIN_SAMPLE_COUNT("ADM-ARL-003", "최소 표본 수가 올바르지 않습니다.");

    private final String code;
    private final String message;
}
