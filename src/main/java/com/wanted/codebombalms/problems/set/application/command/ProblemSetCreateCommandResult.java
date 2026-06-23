package com.wanted.codebombalms.problems.set.application.command;

import com.wanted.codebombalms.problems.set.domain.model.CreatedProblemSummary;
import java.util.List;

public record ProblemSetCreateCommandResult(
        Long problemSetId,
        String title,
        String categoryName,
        Integer totalProblemCount,
        Integer createdProblemCount,
        Integer createdTestCaseCount,
        List<CreatedProblemSummary> problems
) {
}
