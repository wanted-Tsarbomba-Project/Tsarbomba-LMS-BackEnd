package com.wanted.codebombalms.problems.set.application.command;

public record ProblemSetUpdateCommandResult(
        Long problemSetId,
        String title,
        String categoryName,
        Integer updatedProblemCount
) {
}
