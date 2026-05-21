package com.wanted.codebombalms.problems.progress.presentation.api.response;

import com.wanted.codebombalms.problems.progress.domain.model.ProblemProgress;

import java.util.List;

public record ProblemProgressResponse(
        Long problemSetId,
        Integer totalProblemCount,
        Integer currentProblemNumber,
        Long currentProblemId,
        Integer solvedProblemCount,
        List<ProblemProgressItemResponse> problems
) {
    public ProblemProgressResponse(ProblemProgress progress) {
        this(
                progress.getProblemSetId(),
                progress.getTotalProblemCount(),
                progress.getCurrentProblemNumber(),
                progress.getCurrentProblemId(),
                progress.getSolvedProblemCount(),
                progress.getProblems()
                        .stream()
                        .map(ProblemProgressItemResponse::new)
                        .toList()
        );
    }
}
