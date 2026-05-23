package com.wanted.codebombalms.problems.set.presentation.api.request;

import java.util.List;

public record ProblemSetUpdateRequest(
        String title,
        String categoryName,
        String description,
        String difficulty,
        String dataFileName,
        List<ProblemUpdateRequest> problems
) {
}
