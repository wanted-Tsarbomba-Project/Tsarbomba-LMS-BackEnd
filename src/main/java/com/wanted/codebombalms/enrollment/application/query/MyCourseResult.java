package com.wanted.codebombalms.enrollment.application.query;

import com.wanted.codebombalms.enrollment.application.port.CoursePublicationStatus;
import com.wanted.codebombalms.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import java.time.LocalDateTime;

public record MyCourseResult(
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

    public static MyCourseResult from(Enrollment enrollment, CoursePublicationStatus course) {
        return new MyCourseResult(
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
