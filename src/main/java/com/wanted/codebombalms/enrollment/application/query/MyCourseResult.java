package com.wanted.codebombalms.enrollment.application.query;

import com.wanted.codebombalms.enrollment.application.port.CoursePublicationStatus;
import com.wanted.codebombalms.enrollment.application.port.EnrollmentLearningProgressPort.EnrollmentLearningProgress;
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
        LocalDateTime enrolledAt,
        boolean learningCompleted,
        String displayStatus,
        int lectureProgressRate,
        long completedLectureCount,
        long totalLectureCount,
        long completedProblemCount,
        long totalProblemCount
) {

    public static MyCourseResult from(
            Enrollment enrollment,
            CoursePublicationStatus course,
            EnrollmentLearningProgress progress
    ) {
        return new MyCourseResult(
                enrollment.getEnrollmentId(),
                enrollment.getUserId(),
                enrollment.getCourseId(),
                course.instructorId(),
                course.title(),
                course.description(),
                course.thumbnailUrl(),
                enrollment.getStatus(),
                enrollment.getEnrolledAt(),
                progress.learningCompleted(),
                progress.displayStatus(),
                progress.lectureProgressRate(),
                progress.completedLectureCount(),
                progress.totalLectureCount(),
                progress.completedProblemCount(),
                progress.totalProblemCount()
        );
    }
}
