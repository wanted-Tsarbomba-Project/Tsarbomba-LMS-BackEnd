package com.wanted.codebombalms.enrollment.presentation.api.response;

import com.wanted.codebombalms.enrollment.application.query.MyCourseResult;
import com.wanted.codebombalms.enrollment.application.port.CoursePublicationStatus;
import com.wanted.codebombalms.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;

import java.time.LocalDateTime;

public record MyCourseResponse(
        Long enrollmentId,
        Long studentId,
        Long courseId,
        Long instructorId,
        String courseTitle,
        String courseDescription,
        String courseThumbnailUrl,
        EnrollmentStatus status,
        LocalDateTime enrolledAt
) {

    public static MyCourseResponse from(MyCourseResult result) {
        return new MyCourseResponse(
                result.enrollmentId(),
                result.studentId(),
                result.courseId(),
                result.instructorId(),
                result.courseTitle(),
                result.courseDescription(),
                result.courseThumbnailUrl(),
                result.status(),
                result.enrolledAt()
        );
    }

    public static MyCourseResponse from(Enrollment enrollment, CoursePublicationStatus course) {
        return new MyCourseResponse(
                enrollment.getEnrollmentId(),
                enrollment.getUserId(),
                enrollment.getCourseId(),
                course.instructorId(),
                course.title(),
                course.description(),
                course.thumbnailUrl(),
                enrollment.getStatus(),
                enrollment.getEnrolledAt()
        );
    }
}
