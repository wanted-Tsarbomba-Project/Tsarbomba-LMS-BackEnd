package com.wanted.codebombalms.problems.set.presentation.api.response;

import com.wanted.codebombalms.problems.set.application.usecase.EnterProblemSetUseCase.ProblemSetEntryView;

public record ProblemSetEnterResponse(
        Long problemSetId,
        String title,
        String description,
        Integer currentProblemNumber,
        Boolean isCompleted,
        ProblemDetailResponse problem
) {
    public ProblemSetEnterResponse(ProblemSetEntryView entry) {
        this(
                entry.problemSetId(),
                entry.title(),
                entry.description(),
                entry.currentProblemNumber(),
                entry.isCompleted(),
                entry.problem() == null ? null : new ProblemDetailResponse(entry.problem())
        );
    }
}
