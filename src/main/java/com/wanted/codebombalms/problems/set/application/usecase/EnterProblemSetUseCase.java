package com.wanted.codebombalms.problems.set.application.usecase;

import com.wanted.codebombalms.problems.set.application.query.EnterProblemSetQuery;

import java.util.List;

public interface EnterProblemSetUseCase {

    ProblemSetEntryView handle(EnterProblemSetQuery query);

    record ProblemSetEntryView(
            Long problemSetId,
            String title,
            String description,
            Integer currentProblemNumber,
            Long currentProblemId,
            Integer totalProblemCount,
            Integer solvedProblemCount,
            Boolean isCompleted,
            List<ProblemDetailItemView> problems
    ) {
    }

    record ProblemDetailItemView(
            Long problemId,
            Integer problemNumber,
            String title,
            String content,
            String problemType,
            Integer point,
            String startCode,
            String status,
            Long latestSubmissionId
    ) {
    }
}
