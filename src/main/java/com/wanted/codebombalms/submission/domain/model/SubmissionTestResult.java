package com.wanted.codebombalms.submission.domain.model;

public record SubmissionTestResult(
        Long testCaseId,
        Boolean passed,
        String actualOutput,
        String errorMessage,
        Integer executionTimeMs,
        Integer score
) {
}
