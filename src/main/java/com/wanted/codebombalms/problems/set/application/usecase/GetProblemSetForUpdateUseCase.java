package com.wanted.codebombalms.problems.set.application.usecase;

import com.wanted.codebombalms.problems.set.application.query.GetProblemSetForUpdateQuery;

public interface GetProblemSetForUpdateUseCase {

    ProblemSetForUpdateView handle(GetProblemSetForUpdateQuery query);

    record ProblemSetForUpdateView(
            Long problemSetId,
            String title,
            String categoryName,
            String difficulty,
            String description,
            String dataFileName,
            Long datasetId,
            java.util.List<ProblemForUpdateView> problems
    ) {
    }

    record ProblemForUpdateView(
            Long problemId,
            String title,
            String content,
            Integer point,
            String startCode,
            Long hintId,
            String hint,
            String explanation,
            java.util.List<TestCaseForUpdateView> testCases
    ) {
    }

    record TestCaseForUpdateView(
            Long testCaseId,
            String testCode,
            Boolean isHidden,
            Integer timeoutMs
    ) {
    }
}
