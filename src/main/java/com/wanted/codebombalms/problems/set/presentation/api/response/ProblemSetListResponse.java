package com.wanted.codebombalms.problems.set.presentation.api.response;

import com.wanted.codebombalms.problems.set.domain.model.ProblemSetSummary;

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
    public ProblemSetListResponse(ProblemSetSummary problemSet) {
        this(
                problemSet.getProblemSetId(),
                problemSet.getProblemNumber(),
                problemSet.getTitle(),
                problemSet.getDescription(),
                problemSet.getDifficulty(),
                problemSet.getAccuracyRate(),
                problemSet.getCreatedAt()
        );
    }
}
