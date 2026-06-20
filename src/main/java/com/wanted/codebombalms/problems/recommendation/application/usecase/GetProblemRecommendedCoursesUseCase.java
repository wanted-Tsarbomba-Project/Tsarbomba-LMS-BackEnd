package com.wanted.codebombalms.problems.recommendation.application.usecase;

import com.wanted.codebombalms.problems.recommendation.application.query.GetProblemRecommendedCoursesQuery;

import java.util.List;

public interface GetProblemRecommendedCoursesUseCase {

    List<RecommendedCourseView> handle(GetProblemRecommendedCoursesQuery query);

    record RecommendedCourseView(
            Long courseId,
            String title,
            String description,
            String thumbnailUrl,
            Integer displayOrder
    ) {
    }
}
