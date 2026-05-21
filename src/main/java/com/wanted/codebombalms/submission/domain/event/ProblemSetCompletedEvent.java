package com.wanted.codebombalms.submission.domain.event;

public record ProblemSetCompletedEvent(
        Long userId,
        Long problemSetId
) {
}
