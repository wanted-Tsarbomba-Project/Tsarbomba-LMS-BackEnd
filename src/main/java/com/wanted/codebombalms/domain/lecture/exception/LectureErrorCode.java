package com.wanted.codebombalms.domain.lecture.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LectureErrorCode implements ErrorCode {

    LECTURE_NOT_FOUND("LEC-001", "강의를 찾을 수 없습니다.");

    private final String code;
    private final String message;
}
