package com.wanted.codebombalms.domain.problems.problem.dto.response;

import com.wanted.codebombalms.domain.problems.problem.entitiy.Problem;

public record ProblemResponse(
        Long problemId,
        Integer problemNumber,
        String title,
        String content,
        String problemType,
        String startCode
) {
    public ProblemResponse(Problem problem) {
        this(
                problem.getProblemId(),
                problem.getProblemOrder(),
                problem.getTitle(),
                problem.getContent(),
                problem.getProblemType(),
                null
        );
    }
    public ProblemResponse withStartCode(String startCode) {
        return new ProblemResponse(
                problemId,
                problemNumber,
                title,
                content,
                problemType,
                startCode
        );
    }
}
