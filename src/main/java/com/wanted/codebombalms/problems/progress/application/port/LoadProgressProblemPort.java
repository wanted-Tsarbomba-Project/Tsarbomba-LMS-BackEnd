package com.wanted.codebombalms.problems.progress.application.port;

import com.wanted.codebombalms.problems.progress.domain.model.ProblemProgressItem;

import java.util.List;

public interface LoadProgressProblemPort {

    List<ProblemProgressItem> loadProgressProblems(
            Long userId,
            Long problemSetId,
            Integer currentProblemNumber
    );
}
