package com.wanted.codebombalms.domain.problems.set.dto.response;

public record ProblemSetUpdateResponse(
        Long problemSetId,
        String title,
        String categoryName,
        Integer updatedProblemCount
) {
}
