package com.wanted.codebombalms.submission.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public record CodeSubmissionResult(
        Long submissionId,
        Long problemId,
        String submittedCode,
        Boolean correct,
        Integer passedTestCount,
        Integer totalTestCount,
        String executionStatus,
        String errorMessage,
        LocalDateTime submittedAt,
        List<CodeSubmissionTestCaseResult> testCaseResults
) {
}
