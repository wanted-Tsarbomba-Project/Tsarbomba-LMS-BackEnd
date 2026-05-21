package com.wanted.codebombalms.lecture.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LectureErrorCode implements ErrorCode {

    LECTURE_NOT_FOUND("LCT-001", "존재하지 않는 강의입니다."),
    LECTURE_DELETE_STATUS_REQUIRES_DELETE("LCT-002", "강의 삭제는 삭제 기능을 통해서만 가능합니다.");

    private final String code;
    private final String message;
}
