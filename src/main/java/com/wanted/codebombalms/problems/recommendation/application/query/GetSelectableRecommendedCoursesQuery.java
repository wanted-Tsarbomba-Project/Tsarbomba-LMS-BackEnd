package com.wanted.codebombalms.problems.recommendation.application.query;

public record GetSelectableRecommendedCoursesQuery(
        String keyword,
        Integer limit
) {
}
