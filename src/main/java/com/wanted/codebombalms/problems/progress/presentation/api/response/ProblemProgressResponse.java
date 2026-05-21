package com.wanted.codebombalms.problems.progress.presentation.api.response;

import com.wanted.codebombalms.problems.progress.application.usecase.GetProblemProgressUseCase.ProblemProgressView;

import java.util.List;

public record ProblemProgressResponse(
        Long problemSetId,
        Integer totalProblemCount,
        Integer currentProblemNumber,
        Long currentProblemId,
        Integer solvedProblemCount,
        List<ProblemProgressItemResponse> problems
) {
    public ProblemProgressResponse(ProblemProgressView progress) {
        this(
                progress.problemSetId(),
                progress.totalProblemCount(),
                progress.currentProblemNumber(),
                progress.currentProblemId(),
                progress.solvedProblemCount(),
                progress.problems()
                        .stream()
                        .map(ProblemProgressItemResponse::new)
                        .toList()
        );
    }
}
