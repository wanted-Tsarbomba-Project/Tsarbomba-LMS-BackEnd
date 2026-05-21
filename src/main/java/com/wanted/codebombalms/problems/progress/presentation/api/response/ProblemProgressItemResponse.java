package com.wanted.codebombalms.problems.progress.presentation.api.response;

import com.wanted.codebombalms.problems.progress.application.usecase.GetProblemProgressUseCase.ProblemProgressItemView;

public record ProblemProgressItemResponse(
        Long problemId,
        Integer problemNumber,
        String status
) {
    public ProblemProgressItemResponse(ProblemProgressItemView item) {
        this(
                item.problemId(),
                item.problemNumber(),
                item.status()
        );
    }
}
