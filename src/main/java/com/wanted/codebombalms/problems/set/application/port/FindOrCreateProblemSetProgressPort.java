package com.wanted.codebombalms.problems.set.application.port;

import com.wanted.codebombalms.problems.set.domain.model.ProblemSetProgressState;

public interface FindOrCreateProblemSetProgressPort {

    ProblemSetProgressState findOrCreateProgress(Long userId, Long problemSetId);
}
