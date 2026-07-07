package com.wanted.codebombalms.course.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourseErrorCode implements ErrorCode {

    COURSE_NOT_FOUND("CRS-001", "존재하지 않는 강좌입니다."),
    COURSE_LECTURE_REQUIRED("CRS-002", "강좌를 개설하려면 강의가 1개 이상 필요합니다."),
    COURSE_ACTIVE_STATUS_REQUIRES_PUBLISH("CRS-003", "작성 중인 강좌 활성화는 개설 기능을 통해서만 가능합니다."),
    COURSE_NOT_PUBLISHABLE_STATUS("CRS-004", "작성 중인 강좌만 개설할 수 있습니다."),
    COURSE_DELETE_STATUS_REQUIRES_DELETE("CRS-005", "강좌 삭제는 삭제 기능을 통해서만 가능합니다."),
    COURSE_OPERATOR_REQUIRED("CRS-006", "강좌는 운영자만 생성할 수 있습니다."),
    COURSE_CATEGORY_REQUIRED("CRS-007", "활성화된 강좌 카테고리를 선택해야 합니다."),
    COURSE_PROBLEM_SET_REQUIRED("CRS-008", "강좌에 연결할 문제세트가 필요합니다."),
    COURSE_PROBLEM_STEP_REQUIRED("CRS-009", "강좌 문제 연결 단계 정보가 필요합니다."),
    COURSE_PROBLEM_SET_NOT_FOUND("CRS-010", "존재하지 않는 문제세트입니다."),
    COURSE_PROBLEM_NOT_FOUND("CRS-011", "선택한 문제세트에 존재하지 않는 문제입니다."),
    COURSE_PROBLEM_LECTURE_REQUIRED("CRS-012", "MAIN 문제 단계에는 강의가 필요합니다."),
    COURSE_PROBLEM_LECTURE_NOT_FOUND("CRS-013", "선택한 강좌에 존재하지 않는 강의입니다."),
    COURSE_FINAL_PROBLEM_LECTURE_NOT_ALLOWED("CRS-014", "FINAL 문제 단계에는 강의를 지정할 수 없습니다."),
    COURSE_THUMBNAIL_INVALID_FILE("CRS-015", "올바른 강좌 썸네일 이미지 파일이 아닙니다."),
    COURSE_THUMBNAIL_UPLOAD_FAILED("CRS-016", "강좌 썸네일 이미지 업로드에 실패했습니다."),
    COURSE_THUMBNAIL_DELETE_FAILED("CRS-017", "강좌 썸네일 이미지 삭제에 실패했습니다.");

    private final String code;
    private final String message;
}
