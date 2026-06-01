package com.wanted.codebombalms.submission.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubmissionErrorCode implements ErrorCode {

    INVALID_CODE("SUB-001", "코드 값이 비어 있습니다."),
    INVALID_ANSWER("SUB-002", "답안 값이 비어 있습니다."),
    PROBLEM_NOT_RETRIABLE("SUB-003", "재시도할 수 없는 문제입니다."),
    ATTEMPT_LIMIT_EXCEEDED("SUB-004", "제출 가능한 횟수를 초과했습니다."),
    SUBMISSION_NOT_FOUND("SUB-005", "제출 기록을 찾을 수 없습니다.");

    private final String code;
    private final String message;
}
