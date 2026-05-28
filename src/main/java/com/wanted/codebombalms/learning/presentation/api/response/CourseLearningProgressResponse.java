package com.wanted.codebombalms.learning.presentation.api.response;

import com.wanted.codebombalms.learning.domain.model.CourseLearningProgress;

public record CourseLearningProgressResponse(
        Long courseId,
        String courseTitle,
        long enrolledStudentCount,
        long completedLectureCount,
        long totalLectureCount,
        int averageLectureProgressRate,
        long completedProblemCount,
        long totalProblemCount
) {

    public static CourseLearningProgressResponse from(CourseLearningProgress progress) {
        return new CourseLearningProgressResponse(
                progress.courseId(),
                progress.courseTitle(),
                progress.enrolledStudentCount(),
                progress.completedLectureCount(),
                progress.totalLectureCount(),
                progress.averageLectureProgressRate(),
                progress.completedProblemCount(),
                progress.totalProblemCount()
        );
    }
}
