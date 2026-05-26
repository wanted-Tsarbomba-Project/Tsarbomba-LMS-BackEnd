package com.wanted.codebombalms.problems.set.presentation.api.response;

import com.wanted.codebombalms.problems.set.application.usecase.EnterProblemSetUseCase.ProblemDetailView;

public record ProblemDetailResponse(
        Long problemId,
        Integer problemNumber,
        String title,
        String content,
        String problemType,
        Integer point,
        String startCode
) {
    public ProblemDetailResponse(ProblemDetailView problem) {
        this(
                problem.problemId(),
                problem.problemNumber(),
                problem.title(),
                problem.content(),
                problem.problemType(),
                problem.point(),
                problem.startCode()
        );
    }
}