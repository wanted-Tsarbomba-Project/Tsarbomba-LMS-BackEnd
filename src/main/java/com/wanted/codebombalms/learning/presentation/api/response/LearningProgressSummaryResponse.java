package com.wanted.codebombalms.learning.presentation.api.response;

import com.wanted.codebombalms.learning.domain.model.LearningProgressSummary;

public record LearningProgressSummaryResponse(
        long totalCourseCount,
        long totalStudentCount,
        long completedLectureCount,
        long totalLectureCount,
        int averageLectureProgressRate,
        long completedProblemCount,
        long totalProblemCount
) {

    public static LearningProgressSummaryResponse from(LearningProgressSummary summary) {
        return new LearningProgressSummaryResponse(
                summary.totalCourseCount(),
                summary.totalStudentCount(),
                summary.completedLectureCount(),
                summary.totalLectureCount(),
                summary.averageLectureProgressRate(),
                summary.completedProblemCount(),
                summary.totalProblemCount()
        );
    }
}
