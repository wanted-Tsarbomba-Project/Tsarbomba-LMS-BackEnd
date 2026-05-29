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
            String answer,
            Long hintId,
            String hint,
            String explanation
    ) {
        public ProblemForUpdateResponse(ProblemForUpdateView view) {
            this(
                    view.problemId(),
                    view.title(),
                    view.content(),
                    view.point(),
                    view.startCode(),
                    view.answer(),
                    view.hintId(),
                    view.hint(),
                    view.explanation()
            );
        }
    }
}
