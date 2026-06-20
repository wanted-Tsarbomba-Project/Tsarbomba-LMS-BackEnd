package com.wanted.codebombalms.problems.recommendation.application.port;

import java.util.List;

public interface SaveProblemRecommendedCoursePort {

    void replaceRecommendedCourses(Long problemId, List<RecommendedCourseOrder> courses);

    record RecommendedCourseOrder(
            Long courseId,
            Integer displayOrder
    ) {
    }
}
