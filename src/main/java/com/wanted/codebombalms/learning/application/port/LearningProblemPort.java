package com.wanted.codebombalms.learning.application.port;

public interface LearningProblemPort {

    ProblemForLearning loadProblem(Long problemId);

    record ProblemForLearning(
            Long problemId,
            String answer,
            String explanation,
            Integer attemptLimit,
            Boolean retriable
    ) {
    }
}