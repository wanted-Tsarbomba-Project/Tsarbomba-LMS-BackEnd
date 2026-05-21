package com.wanted.codebombalms.problems.set.presentation.api.response;

import com.wanted.codebombalms.problems.set.application.command.ProblemSetDeleteCommandResult;

public record ProblemSetDeleteResponse(
        Long problemSetId,
        String status,
        int deactivatedProblemCount
) {
    public ProblemSetDeleteResponse(ProblemSetDeleteCommandResult result) {
        this(
                result.problemSetId(),
                result.status(),
                result.deactivatedProblemCount()
        );
    }
}
