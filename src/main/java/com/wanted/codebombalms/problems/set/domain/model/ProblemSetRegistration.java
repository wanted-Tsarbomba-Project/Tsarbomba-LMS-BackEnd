package com.wanted.codebombalms.problems.set.domain.model;

import java.util.List;

public record ProblemSetRegistration(
        String title,
        String categoryName,
        String difficulty,
        String description,
        List<ProblemRegistration> problems
) {
}
