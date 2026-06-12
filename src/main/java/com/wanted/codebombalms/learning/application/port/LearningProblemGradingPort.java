package com.wanted.codebombalms.learning.application.port;

public interface LearningProblemGradingPort {

    GradingResult grade(Long problemSetId, Long problemId, String code);

    record GradingResult(
            boolean correct,
            Integer passedTestCount,
            Integer totalTestCount,
            String executionStatus,
            String errorMessage
    ) {
    }
}
