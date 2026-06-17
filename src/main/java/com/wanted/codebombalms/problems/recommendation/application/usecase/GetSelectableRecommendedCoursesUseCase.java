package com.wanted.codebombalms.problems.recommendation.application.usecase;

import java.util.List;

public interface GetSelectableRecommendedCoursesUseCase {

    List<SelectableRecommendedCourseView> handle();

    record SelectableRecommendedCourseView(
            Long courseId,
            String title,
            String description,
            String thumbnailUrl
    ) {
    }
}
