package com.wanted.codebombalms.problems.recommendation.application.usecase;

import com.wanted.codebombalms.problems.recommendation.application.command.SaveProblemRecommendedCoursesCommand;

public interface SaveProblemRecommendedCoursesUseCase {

    SaveProblemRecommendedCoursesResult handle(SaveProblemRecommendedCoursesCommand command);

    record SaveProblemRecommendedCoursesResult(
            Long problemId,
            Integer connectedCourseCount,
            java.util.List<Long> courseIds
    ) {
    }
}
