package com.wanted.codebombalms.problems.recommendation.application.port;

import java.util.List;
import java.util.Set;

public interface LoadRecommendationCoursePort {

    Set<Long> loadActiveCourseIds(Set<Long> courseIds);

    List<SelectableCourseData> loadSelectableCourses();

    record SelectableCourseData(
            Long courseId,
            String title,
            String description,
            String thumbnailUrl
    ) {
    }
}
