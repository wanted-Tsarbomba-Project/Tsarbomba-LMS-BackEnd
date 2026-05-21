package com.wanted.codebombalms.problems.set.application.command;

public record ProblemSetDeleteCommandResult(
        Long problemSetId,
        String status,
        int deactivatedProblemCount
) {
}
