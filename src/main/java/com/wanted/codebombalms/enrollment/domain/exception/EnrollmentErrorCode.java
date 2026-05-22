package com.wanted.codebombalms.enrollment.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnrollmentErrorCode implements ErrorCode {

    ENROLLMENT_NOT_FOUND("ENR-001", "존재하지 않는 수강 신청입니다."),
    DUPLICATE_ENROLLMENT("ENR-002", "이미 수강 신청한 강좌입니다."),
    ENROLLMENT_ALREADY_CANCELED("ENR-003", "이미 취소된 수강 신청입니다."),
    COURSE_NOT_ENROLLABLE("ENR-004", "개설된 강좌만 수강 신청할 수 있습니다.");

    private final String code;
    private final String message;
}
