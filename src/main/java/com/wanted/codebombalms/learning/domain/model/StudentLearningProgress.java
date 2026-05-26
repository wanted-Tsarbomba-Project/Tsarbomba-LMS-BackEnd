package com.wanted.codebombalms.learning.domain.model;

public record StudentLearningProgress(
        Long userId,
        String studentName,
        long completedLectureCount,
        long totalLectureCount,
        int lectureProgressRate,
        long completedProblemCount,
        long totalProblemCount
) {

    public static StudentLearningProgress of(
            Long userId,
            String studentName,
            long completedLectureCount,
            long totalLectureCount,
            long completedProblemCount,
            long totalProblemCount
    ) {
        int lectureProgressRate = totalLectureCount == 0
                ? 0
                : (int) ((completedLectureCount * 100) / totalLectureCount);

        return new StudentLearningProgress(
                userId,
                studentName,
                completedLectureCount,
                totalLectureCount,
                lectureProgressRate,
                completedProblemCount,
                totalProblemCount
        );
    }
}
