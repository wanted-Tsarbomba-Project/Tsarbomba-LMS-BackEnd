package com.wanted.codebombalms.problems.set.presentation.api.response;

import com.wanted.codebombalms.problems.set.domain.model.ProblemDetail;

public record ProblemDetailResponse(
        Long problemId,
        Integer problemNumber,
        String title,
        String content,
        String problemType,
        String startCode
) {
    public ProblemDetailResponse(ProblemDetail problem) {
        this(
                problem.getProblemId(),
                problem.getProblemNumber(),
                problem.getTitle(),
                problem.getContent(),
                problem.getProblemType(),
                problem.getStartCode()
        );
    }
}
