package com.wanted.codebombalms.domain.problems.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProblemErrorCode implements ErrorCode {

    // 문제 (PRB-PBL)
    PROBLEM_NOT_FOUND("PRB-PBL-001", "문제를 찾을 수 없습니다."),
    ACCESS_DENIED("PRB-PBL-002", "문제 접근 권한이 없습니다."),
    PROBLEM_NOT_UNLOCKED("PRB-PBL-003", "이전 문제를 먼저 풀어야 합니다."),
    PROBLEM_HAS_SUBMISSION("PRB-PBL-004", "제출 기록이 존재합니다."),
    PROBLEM_NOT_IN_SET("PRB-PBL-005", "해당 문제 세트에 속한 소문제가 아닙니다."),
    PROBLEM_TITLE_REQUIRED("PRB-PBL-006", "소문제 제목은 필수입니다."),
    PROBLEM_CONTENT_REQUIRED("PRB-PBL-007", "소문제 내용은 필수입니다."),
    PROBLEM_ANSWER_REQUIRED("PRB-PBL-008", "소문제 정답은 필수입니다."),
    PROBLEM_REQUIRED("PRB-PBL-009", "소문제는 1개 이상 필요합니다."),

    // 문제 세트 (PRB-SET)
    PROBLEM_SET_NOT_FOUND("PRB-SET-001", "문제 세트를 찾을 수 없습니다."),
    ATTEMPT_LIMIT_EXCEEDED("PRB-SET-002", "제출 가능 횟수를 초과했습니다."),
    ALREADY_COMPLETED("PRB-SET-003", "이미 완료된 문제 세트입니다."),
    PROBLEM_SET_NOT_COMPLETED("PRB-SET-004", "문제 세트를 끝까지 풀지 않았습니다."),
    NO_CURRENT_PROBLEM("PRB-SET-005", "현재 풀 문제가 없습니다."),
    PROBLEM_SET_TITLE_REQUIRED("PRB-SET-006", "문제 세트 제목은 필수입니다."),

    // 카테고리 (PRB-CAT)
    CATEGORY_NOT_FOUND("PRB-CAT-001", "문제 분야를 찾을 수 없습니다."),
    INVALID_CATEGORY("PRB-CAT-002", "잘못된 문제 분야입니다."),
    PROBLEM_CATEGORY_REQUIRED("PRB-CAT-003", "카테고리는 필수입니다."),

    // 힌트 (PRB-HNT)
    HINT_NOT_FOUND("PRB-HNT-001", "존재하지 않는 힌트입니다."),
    HINT_NOT_IN_PROBLEM("PRB-HNT-002", "해당 소문제에 속한 힌트가 아닙니다."),

    // 데이터셋 (PRB-DAT)
    PROBLEM_DATASET_NOT_FOUND("PRB-DAT-001", "데이터셋을 찾을 수 없습니다."),
    PROBLEM_DATASET_ALREADY_CONNECTED("PRB-DAT-002", "이미 문제에 연결된 데이터셋입니다."),
    PROBLEM_DATASET_INVALID_PROBLEM_TYPE("PRB-DAT-003", "코드 실행형 문제에만 데이터셋을 연결할 수 있습니다."),
    PROBLEM_DATASET_INVALID_FILE("PRB-DAT-004", "CSV 파일만 업로드할 수 있습니다."),
    PROBLEM_DATASET_UPLOAD_FAILED("PRB-DAT-005", "데이터셋 업로드에 실패했습니다."),

    // 공통 (PRB)
    INVALID_INPUT("PRB-001", "필수값이 누락되었습니다."),
    SERVER_ERROR("PRB-002", "문제 도메인 처리 중 서버 오류가 발생했습니다.");

    private final String code;
    private final String message;
}
