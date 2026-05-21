package com.wanted.codebombalms.problems.set.application.command;

public record ProblemSetCreateCommandResult(
        Long problemSetId,
        String title,
        String categoryName,
        Integer totalProblemCount,
        Integer createdProblemCount
) {
}
