package com.wanted.codebombalms.problems.progress.application.port;

import java.util.List;

public interface LoadProblemsForProgressPort {

    List<ProgressProblem> loadActiveProblems(Long problemSetId);

    record ProgressProblem(
            Long problemId,
            Integer problemOrder,
            String title
    ) {
    }
}
