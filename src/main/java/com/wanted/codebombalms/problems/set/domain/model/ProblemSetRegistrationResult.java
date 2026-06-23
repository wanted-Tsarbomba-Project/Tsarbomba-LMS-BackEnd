package com.wanted.codebombalms.problems.set.domain.model;

import java.util.List;

public record ProblemSetRegistrationResult(
        Long problemSetId,
        String title,
        String categoryName,
        Integer totalProblemCount,
        Integer createdProblemCount,
        Integer createdTestCaseCount,
        List<CreatedProblemSummary> problems
) {
}
