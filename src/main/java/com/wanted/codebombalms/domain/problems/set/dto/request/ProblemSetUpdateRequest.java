package com.wanted.codebombalms.domain.problems.set.dto.request;

import java.util.List;

public record ProblemSetUpdateRequest(
        String title,
        String categoryName,
        String description,
        String dataFileName,
        List<ProblemUpdateRequest> problems
) {
}
