package com.wanted.codebombalms.problems.set.presentation.api.response;

import com.wanted.codebombalms.problems.set.domain.model.ProblemSetEntry;

public record ProblemSetEnterResponse(
        Long problemSetId,
        String title,
        String description,
        Integer currentProblemNumber,
        Boolean isCompleted,
        ProblemDetailResponse problem
) {
    public ProblemSetEnterResponse(ProblemSetEntry entry) {
        this(
                entry.getProblemSetId(),
                entry.getTitle(),
                entry.getDescription(),
                entry.getCurrentProblemNumber(),
                entry.getCompleted(),
                entry.getProblem() == null ? null : new ProblemDetailResponse(entry.getProblem())
        );
    }
}
