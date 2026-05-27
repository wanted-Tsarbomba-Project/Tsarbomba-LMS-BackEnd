package com.wanted.codebombalms.learning.presentation.api.response;

import com.wanted.codebombalms.learning.domain.model.LectureProblemStatistics;

public record LectureProblemStatisticsResponse(
        Long lectureId,
        long completedProblemSetCount,
        long totalProblemSetCount,
        int completionRate
) {

    public static LectureProblemStatisticsResponse from(LectureProblemStatistics statistics) {
        return new LectureProblemStatisticsResponse(
                statistics.lectureId(),
                statistics.completedProblemSetCount(),
                statistics.totalProblemSetCount(),
                statistics.completionRate()
        );
    }
}
