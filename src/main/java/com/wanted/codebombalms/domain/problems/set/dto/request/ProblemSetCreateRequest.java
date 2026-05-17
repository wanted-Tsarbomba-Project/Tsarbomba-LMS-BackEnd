package com.wanted.codebombalms.domain.problems.set.dto.request;

import java.util.List;

public record ProblemSetCreateRequest(
        String title,
        String categoryName,
        String description,
        String dataFileName,
        List<ProblemCreateRequest> problems
) {
}
