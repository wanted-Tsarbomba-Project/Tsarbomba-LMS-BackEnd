package com.wanted.codebombalms.submission.domain.event;

public record ProblemSolvedEvent(
        Long userId,
        Long problemId,
        Long submissionId,
        Integer point
) {
}
