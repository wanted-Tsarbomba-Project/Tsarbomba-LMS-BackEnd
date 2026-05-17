package com.wanted.codebombalms.domain.problems.set.dto.response;

import com.wanted.codebombalms.domain.problems.set.entity.ProblemSet;

public record ProblemSetCreateResponse(
        Long problemSetId,
        String title,
        String categoryName,
        Integer totalProblemCount,
        Integer createdProblemCount
) {
    public ProblemSetCreateResponse(ProblemSet problemSet, Integer createdProblemCount) {
        this(
                problemSet.getProblemSetId(),
                problemSet.getTitle(),
                problemSet.getCategory().getCategoryName(),
                problemSet.getTotalProblemCount(),
                createdProblemCount
        );
    }
}