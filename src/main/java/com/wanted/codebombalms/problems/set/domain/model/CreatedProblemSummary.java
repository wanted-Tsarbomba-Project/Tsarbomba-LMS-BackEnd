package com.wanted.codebombalms.problems.set.domain.model;

public record CreatedProblemSummary(
        Long problemId,
        Integer problemOrder,
        String title
) {
}
