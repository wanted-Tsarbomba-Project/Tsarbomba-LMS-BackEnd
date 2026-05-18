package com.wanted.codebombalms.domain.problems.exception;

import com.wanted.codebombalms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProblemErrorCode implements ErrorCode {

    PROBLEM_NOT_FOUND(404, "P-001", "존재하지 않는 문제입니다."),
    PROBLEM_SET_NOT_FOUND(404, "P-002", "존재하지 않는 문제세트입니다."),
    CATEGORY_NOT_FOUND(404, "P-003", "존재하지 않는 카테고리입니다."),
    ATTEMPT_LIMIT_EXCEEDED(400, "P-004", "시도 횟수를 초과했습니다."),
    ALREADY_COMPLETED(409, "P-005", "이미 완료된 문제집입니다."),
    PROBLEM_NOT_UNLOCKED(400, "P-006", "아직 열리지 않은 문제입니다."),
    PROBLEM_SET_NOT_COMPLETED(400, "P-007", "문제 세트를 완료하지 않았습니다."),
    NO_CURRENT_PROBLEM(404, "P-008", "현재 풀 문제가 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}