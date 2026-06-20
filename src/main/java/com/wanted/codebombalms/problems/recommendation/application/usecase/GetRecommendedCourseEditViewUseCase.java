package com.wanted.codebombalms.problems.recommendation.application.usecase;

import com.wanted.codebombalms.problems.recommendation.application.query.GetRecommendedCourseEditViewQuery;

import java.util.List;

public interface GetRecommendedCourseEditViewUseCase {

    RecommendedCourseEditView handle(GetRecommendedCourseEditViewQuery query);

    record RecommendedCourseEditView(
            Long problemId,
            List<Long> selectedCourseIds,
            List<SelectableCourseView> courses
    ) {
    }

    record SelectableCourseView(
            Long courseId,
            String title,
            String description,
            String thumbnailUrl,
            Boolean selected,
            Integer displayOrder
    ) {
    }
}
