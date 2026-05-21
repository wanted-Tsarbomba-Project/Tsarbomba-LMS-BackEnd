package com.wanted.codebombalms.problems.set.domain.model;

public record ProblemSetModificationResult(
        Long problemSetId,
        String title,
        String categoryName,
        Integer updatedProblemCount
) {
}
