package com.wanted.codebombalms.domain.course.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourseErrorCode implements ErrorCode {

    COURSE_NOT_FOUND(404, "CR-001", "존재하지 않는 강좌입니다.");

    private final int status;
    private final String code;
    private final String message;
}