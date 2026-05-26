package com.wanted.codebombalms.submission.domain.model;

public record CodeSubmission(
        Long userId,
        Long problemId,
        String submittedCode,
        Boolean correct,
        Integer attemptNo,
        Integer passedTestCount,
        Integer totalTestCount,
        String executionStatus,
        String errorMessage
) {
}