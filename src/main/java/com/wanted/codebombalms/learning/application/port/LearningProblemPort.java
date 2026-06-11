package com.wanted.codebombalms.learning.application.port;

public interface LearningProblemPort {

    ProblemForLearning loadProblem(Long problemId);

    boolean existsProblemInSet(Long problemSetId, Long problemId);

    record ProblemForLearning(
            Long problemId,
            String explanation,
            Integer attemptLimit,
            Boolean retriable
    ) {
    }
}
