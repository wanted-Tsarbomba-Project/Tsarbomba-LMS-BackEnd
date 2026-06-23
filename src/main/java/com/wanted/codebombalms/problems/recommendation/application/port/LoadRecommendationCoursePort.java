package com.wanted.codebombalms.problems.recommendation.application.port;

import java.util.List;
import java.util.Set;

public interface LoadRecommendationCoursePort {

    Set<Long> loadActiveCourseIds(Set<Long> courseIds);

    List<SelectableCourseData> loadSelectableCourses(String keyword, int limit);

    List<SelectableCourseData> loadActiveCoursesByIds(Set<Long> courseIds);

    record SelectableCourseData(
            Long courseId,
            Long categoryId,
            String categoryName,
            String title,
            String description,
            String thumbnailUrl
    ) {
    }
}
