package com.wanted.codebombalms.domain.problems.set.dto.response;

public record ProblemSetDeleteResponse(
        Long problemSetId,
        String status,
        int deactivatedProblemCount
) {
}
