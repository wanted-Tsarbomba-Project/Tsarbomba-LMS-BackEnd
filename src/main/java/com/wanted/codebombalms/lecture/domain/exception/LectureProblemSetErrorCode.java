package com.wanted.codebombalms.lecture.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LectureProblemSetErrorCode implements ErrorCode {

    PROBLEM_SET_REQUIRED("CRS-008", "강좌에 연결할 문제세트가 필요합니다."),
    PROBLEM_STEP_REQUIRED("CRS-009", "강좌 문제 연결 단계 정보가 필요합니다."),
    PROBLEM_SET_NOT_FOUND("CRS-010", "존재하지 않는 문제세트입니다."),
    MAIN_LECTURE_REQUIRED("CRS-012", "MAIN 문제 단계에는 강의가 필요합니다."),
    LECTURE_NOT_IN_COURSE("CRS-013", "선택한 강좌에 존재하지 않는 강의입니다."),
    FINAL_LECTURE_NOT_ALLOWED("CRS-014", "FINAL 문제 단계에는 강의를 지정할 수 없습니다.");

    private final String code;
    private final String message;
}
