package com.wanted.codebombalms.problems.progress.application.usecase;

import com.wanted.codebombalms.problems.progress.application.query.GetProblemProgressQuery;

import java.util.List;

public interface GetProblemProgressUseCase {

    ProblemProgressView handle(GetProblemProgressQuery query);

    record ProblemProgressView(
            Long problemSetId,
            Integer totalProblemCount,
            Integer currentProblemNumber,
            Long currentProblemId,
            Integer solvedProblemCount,
            List<ProblemProgressItemView> problems
    ) {
    }

    record ProblemProgressItemView(
            Long problemId,
            Integer problemNumber,
            String status
    ) {
    }
}
