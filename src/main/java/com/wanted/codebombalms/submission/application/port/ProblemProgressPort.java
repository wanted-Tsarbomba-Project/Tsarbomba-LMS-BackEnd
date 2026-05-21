package com.wanted.codebombalms.submission.application.port;

public interface ProblemProgressPort {

    void validateCurrentProblem(Long userId, Long problemSetId, Integer problemOrder);

    Integer openNextProblem(Long userId, Long problemSetId);

    void completeProblemSet(Long userId, Long problemSetId);
}
