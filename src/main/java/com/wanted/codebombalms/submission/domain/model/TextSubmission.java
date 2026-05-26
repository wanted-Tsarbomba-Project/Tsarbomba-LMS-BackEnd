package com.wanted.codebombalms.submission.domain.model;

public record TextSubmission(
        Long userId,
        Long problemId,
        String submittedAnswer,
        Boolean correct,
        Integer attemptNo
) {
}