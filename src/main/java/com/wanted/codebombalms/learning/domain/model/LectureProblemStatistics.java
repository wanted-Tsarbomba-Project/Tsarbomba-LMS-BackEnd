package com.wanted.codebombalms.learning.domain.model;

public record LectureProblemStatistics(
        Long lectureId,
        long completedProblemSetCount,
        long totalProblemSetCount,
        int completionRate
) {

    public static LectureProblemStatistics of(
            Long lectureId,
            long completedProblemSetCount,
            long totalProblemSetCount
    ) {
        int completionRate = totalProblemSetCount == 0
                ? 0
                : (int) ((completedProblemSetCount * 100) / totalProblemSetCount);

        return new LectureProblemStatistics(
                lectureId,
                completedProblemSetCount,
                totalProblemSetCount,
                completionRate
        );
    }
}
