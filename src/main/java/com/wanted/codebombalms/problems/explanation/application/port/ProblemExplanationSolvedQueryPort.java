package com.wanted.codebombalms.problems.explanation.application.port;

public interface ProblemExplanationSolvedQueryPort {

    boolean existsCorrectSubmission(Long userId, Long problemId);
}
