package com.wanted.codebombalms.problems.result.application.port;

public interface CheckProblemSetCompletionPort {

    void checkCompleted(Long userId, Long problemSetId);
}
