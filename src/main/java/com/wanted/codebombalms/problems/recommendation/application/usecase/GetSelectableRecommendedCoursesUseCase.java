package com.wanted.codebombalms.problems.recommendation.application.usecase;

import com.wanted.codebombalms.problems.recommendation.application.query.GetSelectableRecommendedCoursesQuery;

import java.util.List;

public interface GetSelectableRecommendedCoursesUseCase {

    List<SelectableRecommendedCourseView> handle(GetSelectableRecommendedCoursesQuery query);

    record SelectableRecommendedCourseView(
            Long courseId,
            Long categoryId,
            String categoryName,
            String title,
            String description,
            String thumbnailUrl
    ) {
    }
}
