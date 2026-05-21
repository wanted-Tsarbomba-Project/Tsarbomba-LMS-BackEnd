package com.wanted.codebombalms.problems.progress.presentation.api.response;

import com.wanted.codebombalms.problems.progress.domain.model.ProblemProgressItem;
import com.wanted.codebombalms.problems.progress.enums.ProblemProgressStatus;

public record ProblemProgressItemResponse(
        Long problemId,
        Integer problemNumber,
        ProblemProgressStatus status
) {
    public ProblemProgressItemResponse(ProblemProgressItem item) {
        this(
                item.getProblemId(),
                item.getProblemNumber(),
                item.getStatus()
        );
    }
}
