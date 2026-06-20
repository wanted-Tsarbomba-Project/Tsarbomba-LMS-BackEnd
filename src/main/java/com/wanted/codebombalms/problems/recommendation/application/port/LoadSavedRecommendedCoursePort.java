package com.wanted.codebombalms.problems.recommendation.application.port;

import java.util.List;

public interface LoadSavedRecommendedCoursePort {

    List<SavedRecommendedCourseData> loadSavedRecommendedCourses(Long problemId);

    record SavedRecommendedCourseData(
            Long courseId,
            Integer displayOrder
    ) {
    }
}
