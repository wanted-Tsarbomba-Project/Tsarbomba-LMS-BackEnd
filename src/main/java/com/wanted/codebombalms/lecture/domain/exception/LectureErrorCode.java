package com.wanted.codebombalms.lecture.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LectureErrorCode implements ErrorCode {

    LECTURE_NOT_FOUND("LCT-001", "강의를 찾을 수 없습니다."),
    LECTURE_DELETE_STATUS_REQUIRES_DELETE("LCT-002", "강의 삭제는 삭제 API를 이용해주세요."),
    LECTURE_ORDER_DUPLICATED("LCT-003", "같은 순서의 강의가 이미 존재합니다."),
    INVALID_YOUTUBE_VIDEO_URL("LCT-004", "강의 영상 URL은 올바른 YouTube URL이어야 합니다."),
    LECTURE_MATERIAL_NOT_FOUND("LCT-005", "강의자료를 찾을 수 없습니다."),
    LECTURE_MATERIAL_INVALID_FILE("LCT-006", "유효하지 않은 강의자료 파일입니다."),
    LECTURE_MATERIAL_UPLOAD_FAILED("LCT-007", "강의자료 업로드에 실패했습니다."),
    LECTURE_MATERIAL_DOWNLOAD_URL_FAILED("LCT-008", "강의자료 다운로드 URL 발급에 실패했습니다."),
    LECTURE_MATERIAL_ACCESS_DENIED("LCT-009", "강의자료에 접근할 권한이 없습니다."),
    LECTURE_MATERIAL_DELETE_FAILED("LCT-010", "강의자료 삭제에 실패했습니다."),
    LECTURE_ACCESS_DENIED("LCT-011", "해당 강좌를 수강 중인 학생만 강의에 접근할 수 있습니다."),
    PREVIOUS_LECTURE_NOT_COMPLETED("LCT-012", "이전 강의를 모두 완료해야 이 강의에 접근할 수 있습니다."),
    FINAL_PROBLEM_SET_NOT_AVAILABLE("LCT-013", "마지막 강의를 완료한 후 FINAL 추천 문제세트를 조회할 수 있습니다."),
    INVALID_LECTURE_ORDER_REQUEST("LCT-014", "삭제되지 않은 모든 강의를 중복 없이 한 번씩 포함해야 합니다.");

    private final String code;
    private final String message;
}
