package com.wanted.codebombalms.problems.recommendation.application.command;

import java.util.List;

public record SaveProblemRecommendedCoursesCommand(
        Long problemId,
        List<Long> courseIds
) {
}
