package com.wanted.codebombalms.enrollment.presentation.api.response;

import com.wanted.codebombalms.enrollment.application.query.MyCourseResult;
import com.wanted.codebombalms.enrollment.application.port.CoursePublicationStatus;
import com.wanted.codebombalms.enrollment.application.port.EnrollmentLearningProgressPort.EnrollmentLearningProgress;
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
        LocalDateTime enrolledAt,
        boolean learningCompleted,
        String displayStatus,
        int lectureProgressRate,
        long completedLectureCount,
        long totalLectureCount,
        long completedProblemCount,
        long totalProblemCount
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
                result.enrolledAt(),
                result.learningCompleted(),
                result.displayStatus(),
                result.lectureProgressRate(),
                result.completedLectureCount(),
                result.totalLectureCount(),
                result.completedProblemCount(),
                result.totalProblemCount()
        );
    }

    public static MyCourseResponse from(
            Enrollment enrollment,
            CoursePublicationStatus course,
            EnrollmentLearningProgress progress
    ) {
        return new MyCourseResponse(
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
