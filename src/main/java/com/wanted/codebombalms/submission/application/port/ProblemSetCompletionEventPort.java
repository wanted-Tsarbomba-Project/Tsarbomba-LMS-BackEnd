package com.wanted.codebombalms.submission.application.port;

public interface ProblemSetCompletionEventPort {

    void publishCompleted(Long userId, Long problemSetId);
}
