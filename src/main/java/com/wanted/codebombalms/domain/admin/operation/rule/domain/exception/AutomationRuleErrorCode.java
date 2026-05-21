package com.wanted.codebombalms.domain.admin.operation.rule.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AutomationRuleErrorCode implements ErrorCode {

    INVALID_CREATE_REQUEST(400, "AR-001", "자동화 규칙 등록 요청이 올바르지 않습니다."),
    INVALID_THRESHOLD_VALUE(400, "AR-002", "임계값이 올바르지 않습니다."),
    INVALID_MIN_SAMPLE_COUNT(400, "AR-003", "최소 표본 수가 올바르지 않습니다."),
    DUPLICATED_RULE_CODE(409, "AR-004", "이미 등록된 자동화 규칙입니다.");

    private final int status;
    private final String code;
    private final String message;
}