package com.wanted.codebombalms.learning.domain.model;

public record LearningProgressSummary(
        long totalCourseCount,
        long totalStudentCount,
        long completedLectureCount,
        long totalLectureCount,
        int averageLectureProgressRate,
        long completedProblemCount,
        long totalProblemCount
) {

    public static LearningProgressSummary of(
            long totalCourseCount,
            long totalStudentCount,
            long completedLectureCount,
            long totalLectureCount,
            long completedProblemCount,
            long totalProblemCount
    ) {
        int averageLectureProgressRate = totalLectureCount == 0
                ? 0
                : (int) ((completedLectureCount * 100) / totalLectureCount);

        return new LearningProgressSummary(
                totalCourseCount,
                totalStudentCount,
                completedLectureCount,
                totalLectureCount,
                averageLectureProgressRate,
                completedProblemCount,
                totalProblemCount
        );
    }
}
