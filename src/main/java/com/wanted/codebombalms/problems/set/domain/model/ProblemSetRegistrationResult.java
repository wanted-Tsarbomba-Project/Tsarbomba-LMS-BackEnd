package com.wanted.codebombalms.problems.set.domain.model;

public record ProblemSetRegistrationResult(
        Long problemSetId,
        String title,
        String categoryName,
        Integer totalProblemCount,
        Integer createdProblemCount,
        Integer createdTestCaseCount
) {
}
