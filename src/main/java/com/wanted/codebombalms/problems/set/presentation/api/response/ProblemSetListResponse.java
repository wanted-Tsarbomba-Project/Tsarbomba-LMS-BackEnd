package com.wanted.codebombalms.problems.set.presentation.api.response;

import com.wanted.codebombalms.problems.set.application.usecase.GetProblemSetsUseCase.ProblemSetSummaryView;

import java.time.LocalDateTime;

public record ProblemSetListResponse(
        Long problemSetId,
        Integer problemNumber,
        String title,
        String description,
        String difficulty,
        Double accuracyRate,
        LocalDateTime createdAt
) {
    public ProblemSetListResponse(ProblemSetSummaryView problemSet) {
        this(
                problemSet.problemSetId(),
                problemSet.problemNumber(),
                problemSet.title(),
                problemSet.description(),
                problemSet.difficulty(),
                problemSet.accuracyRate(),
                problemSet.createdAt()
        );
    }
}
