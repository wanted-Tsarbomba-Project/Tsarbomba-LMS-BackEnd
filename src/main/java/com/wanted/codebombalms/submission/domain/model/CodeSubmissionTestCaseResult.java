package com.wanted.codebombalms.submission.domain.model;

public record CodeSubmissionTestCaseResult(
        Long testCaseId,
        Boolean passed,
        Boolean hidden,
        String actualOutput,
        String errorMessage,
        Integer executionTimeMs
) {
}