package com.wanted.codebombalms.problems.set.presentation.api.response;

import com.wanted.codebombalms.problems.set.application.command.ProblemSetCreateCommandResult;

public record ProblemSetCreateResponse(
        Long problemSetId,
        String title,
        String categoryName,
        Integer totalProblemCount,
        Integer createdProblemCount
) {
    public ProblemSetCreateResponse(ProblemSetCreateCommandResult result) {
        this(
                result.problemSetId(),
                result.title(),
                result.categoryName(),
                result.totalProblemCount(),
                result.createdProblemCount()
        );
    }
}
