package com.wanted.codebombalms.problems.set.domain.model;

public record ProblemSetProgressState(
        Integer currentProblemNumber,
        Boolean completed
) {
}
