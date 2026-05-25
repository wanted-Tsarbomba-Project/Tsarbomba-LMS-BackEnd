package com.wanted.codebombalms.learning.application.port;

import java.util.List;
import java.util.Optional;

public interface LearningCourseProblemPort {

    Optional<CourseProblemStepInfo> findCourseProblemStep(Long courseProblemStepId);

    List<Long> findCourseProblemStepIdsByCourse(Long courseId);

    record CourseProblemStepInfo(
            Long courseProblemStepId,
            Long problemId,
            Long lectureId
    ) {
    }
}
