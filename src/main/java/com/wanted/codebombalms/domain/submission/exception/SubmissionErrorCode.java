package com.wanted.codebombalms.domain.submission.exception;

import com.wanted.codebombalms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubmissionErrorCode implements ErrorCode {

    INVALID_ANSWER(400, "PROBLEM_INVALID_ANSWER", "답안 값이 비어 있습니다."),
    PROBLEM_NOT_RETRIABLE(400, "PROBLEM_NOT_RETRIABLE", "재시도할 수 없는 문제입니다.");

    private final int status;
    private final String code;
    private final String message;
}
