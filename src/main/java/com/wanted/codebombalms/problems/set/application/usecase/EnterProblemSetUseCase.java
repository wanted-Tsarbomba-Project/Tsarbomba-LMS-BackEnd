package com.wanted.codebombalms.problems.set.application.usecase;

import com.wanted.codebombalms.problems.set.application.query.EnterProblemSetQuery;

public interface EnterProblemSetUseCase {

    ProblemSetEntryView handle(EnterProblemSetQuery query);

    record ProblemSetEntryView(
            Long problemSetId,
            String title,
            String description,
            Integer currentProblemNumber,
            Boolean isCompleted,
            ProblemDetailView problem
    ) {
    }

    record ProblemDetailView(
            Long problemId,
            Integer problemNumber,
            String title,
            String content,
            String problemType,
            Integer point,
            String startCode
    ) {
    }
}