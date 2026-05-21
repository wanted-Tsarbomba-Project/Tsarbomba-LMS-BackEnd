package com.wanted.codebombalms.problems.set.domain.model;

public record ProblemSetDeactivationResult(
        Long problemSetId,
        String status,
        int deactivatedProblemCount
) {
}
