package com.wanted.codebombalms.domain.problems.exception;

import com.wanted.codebombalms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProblemErrorCode implements ErrorCode {

    PROBLEM_NOT_FOUND(404, "PROBLEM_NOT_FOUND", "문제를 찾을 수 없습니다."),
    PROBLEM_SET_NOT_FOUND(404, "PROBLEM_SET_NOT_FOUND", "문제 세트를 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(404, "PROBLEM_CATEGORY_NOT_FOUND", "문제 분야를 찾을 수 없습니다."),
    INVALID_CATEGORY(400, "PROBLEM_INVALID_CATEGORY", "잘못된 문제 분야입니다."),
    ACCESS_DENIED(403, "PROBLEM_ACCESS_DENIED", "문제 접근 권한이 없습니다."),
    ATTEMPT_LIMIT_EXCEEDED(429, "PROBLEM_ATTEMPT_LIMIT_EXCEEDED", "제출 가능 횟수를 초과했습니다."),
    ALREADY_COMPLETED(409, "PROBLEM_ALREADY_COMPLETED", "이미 완료된 문제 세트입니다."),
    PROBLEM_NOT_UNLOCKED(409, "PROBLEM_NOT_UNLOCKED", "이전 문제를 먼저 풀어야 합니다."),
    PROBLEM_SET_NOT_COMPLETED(400, "PROBLEM_SET_NOT_COMPLETED", "문제 세트를 끝까지 풀지 않았습니다."),
    NO_CURRENT_PROBLEM(404, "PROBLEM_NOT_FOUND", "현재 풀 문제가 없습니다."),
    INVALID_INPUT(400, "PROBLEM_INVALID_INPUT", "필수값이 누락되었습니다."),
    PROBLEM_SET_TITLE_REQUIRED(400, "PROBLEM_INVALID_INPUT", "문제 세트 제목은 필수입니다."),
    PROBLEM_CATEGORY_REQUIRED(400, "PROBLEM_INVALID_INPUT", "카테고리는 필수입니다."),
    PROBLEM_REQUIRED(400, "PROBLEM_INVALID_INPUT", "소문제는 1개 이상 필요합니다."),
    PROBLEM_TITLE_REQUIRED(400, "PROBLEM_INVALID_INPUT", "소문제 제목은 필수입니다."),
    PROBLEM_CONTENT_REQUIRED(400, "PROBLEM_INVALID_INPUT", "소문제 내용은 필수입니다."),
    PROBLEM_ANSWER_REQUIRED(400, "PROBLEM_INVALID_INPUT", "소문제 정답은 필수입니다."),
    PROBLEM_HAS_SUBMISSION(409, "PROBLEM_HAS_SUBMISSION", "제출 기록이 존재합니다."),
    SERVER_ERROR(500, "PROBLEM_SERVER_ERROR", "문제 도메인 처리 중 서버 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
