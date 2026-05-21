package com.wanted.codebombalms.domain.lecture.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LectureErrorCode implements ErrorCode {

    LECTURE_NOT_FOUND("LCT-001", "존재하지 않는 강의입니다.");

    private final String code;
    private final String message;
}
