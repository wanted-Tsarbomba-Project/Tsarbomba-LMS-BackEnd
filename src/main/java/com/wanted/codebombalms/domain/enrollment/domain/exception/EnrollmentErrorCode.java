package com.wanted.codebombalms.domain.enrollment.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnrollmentErrorCode implements ErrorCode {

    // 조회
    ENROLLMENT_NOT_FOUND("ENR-001", "존재하지 않는 수강 신청입니다."),

    // 상태
    DUPLICATE_ENROLLMENT("ENR-002", "이미 수강 신청한 강좌입니다."),
    ENROLLMENT_ALREADY_CANCELED("ENR-003", "이미 취소된 수강 신청입니다.");

    private final String code;
    private final String message;
}
