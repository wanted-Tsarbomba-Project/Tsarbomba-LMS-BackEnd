package com.wanted.codebombalms.problems.progress.application.port;

public interface ProgressManagementPort {

    void validateCurrentProblem(Long userId, Long problemSetId, Integer problemOrder);

    Integer openNextProblem(Long userId, Long problemSetId);

    void completeProblemSet(Long userId, Long problemSetId);
}
