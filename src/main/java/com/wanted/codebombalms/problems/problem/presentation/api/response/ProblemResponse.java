package com.wanted.codebombalms.problems.problem.presentation.api.response;

import com.wanted.codebombalms.problems.problem.application.service.ProblemQueryService.ProblemView;

public record ProblemResponse(
        Long problemId,
        Integer problemNumber,
        String title,
        String content,
        String problemType,
        Integer point,
        String startCode
) {
    public ProblemResponse(ProblemView result) {
        this(
                result.problemId(),
                result.problemNumber(),
                result.title(),
                result.content(),
                result.problemType(),
                result.point(),
                result.startCode()
        );
    }

    public ProblemResponse withStartCode(String startCode) {
        return new ProblemResponse(
                problemId,
                problemNumber,
                title,
                content,
                problemType,
                point,
                startCode
        );
    }
}