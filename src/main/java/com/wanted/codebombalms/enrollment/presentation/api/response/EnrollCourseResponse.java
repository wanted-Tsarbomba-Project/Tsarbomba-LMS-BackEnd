package com.wanted.codebombalms.enrollment.presentation.api.response;

import com.wanted.codebombalms.enrollment.domain.model.Enrollment;

public record EnrollCourseResponse(
        Long enrollmentId
) {

    public static EnrollCourseResponse from(Enrollment enrollment) {
        return new EnrollCourseResponse(enrollment.getEnrollmentId());
    }
}
