package com.wanted.codebombalms.domain.enrollment.domain.exception;

import lombok.Getter;

@Getter
public class DuplicateEnrollmentException extends RuntimeException {

    private final Long courseId;
    private final Long studentId;
    private final EnrollmentErrorCode errorCode;

    public DuplicateEnrollmentException(Long courseId, Long studentId) {
        super(EnrollmentErrorCode.DUPLICATE_ENROLLMENT.getMessage()
                + " courseId: " + courseId
                + ", studentId: " + studentId);
        this.courseId = courseId;
        this.studentId = studentId;
        this.errorCode = EnrollmentErrorCode.DUPLICATE_ENROLLMENT;
    }

    public String getErrorCode() {
        return errorCode.getCode();
    }
}