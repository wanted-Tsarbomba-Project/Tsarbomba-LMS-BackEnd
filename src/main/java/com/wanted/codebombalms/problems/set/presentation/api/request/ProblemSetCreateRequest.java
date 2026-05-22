package com.wanted.codebombalms.problems.set.presentation.api.request;

import java.util.List;

public record ProblemSetCreateRequest(
        String title,
        String categoryName,
        String description,
        String difficulty,
        String dataFileName,
        List<ProblemCreateRequest> problems
) {
}
