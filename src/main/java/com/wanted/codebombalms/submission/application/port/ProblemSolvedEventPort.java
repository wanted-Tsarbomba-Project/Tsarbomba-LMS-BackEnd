package com.wanted.codebombalms.submission.application.port;

public interface ProblemSolvedEventPort {

    void publishSolved(Long userId, Long problemId, Long submissionId, Integer point);
}
