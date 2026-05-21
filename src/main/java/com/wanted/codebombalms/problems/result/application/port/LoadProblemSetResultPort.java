package com.wanted.codebombalms.problems.result.application.port;

import com.wanted.codebombalms.problems.result.domain.model.ProblemSetResult;

public interface LoadProblemSetResultPort {

    ProblemSetResult loadResult(Long userId, Long problemSetId);
}
