package com.wanted.codebombalms.problems.set.presentation.api.response;

import com.wanted.codebombalms.problems.set.application.command.ProblemSetUpdateCommandResult;

public record ProblemSetUpdateResponse(
        Long problemSetId,
        String title,
        String categoryName,
        Integer updatedProblemCount
) {
    public ProblemSetUpdateResponse(ProblemSetUpdateCommandResult result) {
        this(
                result.problemSetId(),
                result.title(),
                result.categoryName(),
                result.updatedProblemCount()
        );
    }
}
