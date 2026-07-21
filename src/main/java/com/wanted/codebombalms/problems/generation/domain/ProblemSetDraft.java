package com.wanted.codebombalms.problems.generation.domain;

import java.util.List;

public record ProblemSetDraft(
        String title,
        String categoryName,
        String difficulty,
        String description,
        String dataFileName,
        List<GeneratedProblemDraft> problems
) {
}
