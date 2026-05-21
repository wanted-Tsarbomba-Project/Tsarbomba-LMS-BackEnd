package com.wanted.codebombalms.submission.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubmissionErrorCode implements ErrorCode {

    // 답안
    INVALID_ANSWER("SUB-001", "답안 값이 비어 있습니다."),

    // 재시도 / 제출 제한
    PROBLEM_NOT_RETRIABLE("SUB-002", "재시도할 수 없는 문제입니다."),
    ATTEMPT_LIMIT_EXCEEDED("SUB-003", "제출 가능 횟수를 초과했습니다.");

    private final String code;
    private final String message;
}
