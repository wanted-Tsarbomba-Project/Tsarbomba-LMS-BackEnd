package com.wanted.codebombalms.domain.enrollment.exception;

import lombok.Getter;

@Getter
public class EnrollmentNotFoundException extends RuntimeException {

    private final Long enrollmentId;
    private final EnrollmentErrorCode errorCode;

    public EnrollmentNotFoundException(Long enrollmentId) {
        super(EnrollmentErrorCode.ENROLLMENT_NOT_FOUND.getMessage() + " enrollmentId: " + enrollmentId);
        this.enrollmentId = enrollmentId;
        this.errorCode = EnrollmentErrorCode.ENROLLMENT_NOT_FOUND;
    }

    public String getErrorCode() {
        return errorCode.getCode();
    }
}