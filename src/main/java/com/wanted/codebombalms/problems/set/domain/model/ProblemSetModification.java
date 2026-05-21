package com.wanted.codebombalms.problems.set.domain.model;

import java.util.List;

public record ProblemSetModification(
        Long problemSetId,
        String title,
        String categoryName,
        String description,
        List<ProblemModification> problems
) {
}
