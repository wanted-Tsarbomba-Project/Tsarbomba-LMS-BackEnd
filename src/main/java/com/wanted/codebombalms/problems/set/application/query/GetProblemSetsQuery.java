package com.wanted.codebombalms.problems.set.application.query;

public record GetProblemSetsQuery(
        Long categoryId,
        int page,
        int size
) {
}
