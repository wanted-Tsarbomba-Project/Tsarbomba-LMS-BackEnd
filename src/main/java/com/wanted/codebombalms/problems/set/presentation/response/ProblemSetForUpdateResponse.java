package com.wanted.codebombalms.problems.set.presentation.response;

import com.wanted.codebombalms.problems.set.application.usecase.GetProblemSetForUpdateUseCase.ProblemForUpdateView;
import com.wanted.codebombalms.problems.set.application.usecase.GetProblemSetForUpdateUseCase.ProblemSetForUpdateView;

import java.util.List;

public record ProblemSetForUpdateResponse(
        Long problemSetId,
        String title,
        String categoryName,
        String difficulty,
        String description,
        String dataFileName,
        Long datasetId,
        String datasetUrl,
        List<ProblemForUpdateResponse> problems
) {
    public ProblemSetForUpdateResponse(ProblemSetForUpdateView view) {
        this(
                view.problemSetId(),
                view.title(),
                view.categoryName(),
                view.difficulty(),
                view.description(),
                view.dataFileName(),
                view.datasetId(),
                view.datasetUrl(),
                view.problems().stream()
                        .map(ProblemForUpdateResponse::new)
                        .toList()
        );
    }

    public record ProblemForUpdateResponse(
            Long problemId,
            String title,
            String content,
            Integer point,
            String startCode,
            Long hintId,
            String hint,
            String explanation,
            List<TestCaseForUpdateResponse> testCases
    ) {
        public ProblemForUpdateResponse(ProblemForUpdateView view) {
            this(
                    view.problemId(),
                    view.title(),
                    view.content(),
                    view.point(),
                    view.startCode(),
                    view.hintId(),
                    view.hint(),
                    view.explanation(),
                    view.testCases().stream()
                            .map(TestCaseForUpdateResponse::new)
                            .toList()
            );
        }
    }

    public record TestCaseForUpdateResponse(
            Long testCaseId,
            String testCode,
            Boolean isHidden,
            Integer timeoutMs
    ) {
        public TestCaseForUpdateResponse(
                com.wanted.codebombalms.problems.set.application.usecase.GetProblemSetForUpdateUseCase.TestCaseForUpdateView view
        ) {
            this(view.testCaseId(), view.testCode(), view.isHidden(), view.timeoutMs());
        }
    }
}
