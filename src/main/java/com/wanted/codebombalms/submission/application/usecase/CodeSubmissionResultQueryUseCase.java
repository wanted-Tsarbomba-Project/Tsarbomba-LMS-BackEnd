package com.wanted.codebombalms.submission.application.usecase;

import java.time.LocalDateTime;
import java.util.List;

public interface CodeSubmissionResultQueryUseCase {

    CodeSubmissionResultView handle(Long submissionId);

    record CodeSubmissionResultView(
            Long submissionId,
            Long problemId,
            Boolean correct,
            Integer earnedScore,
            Integer passedTestCount,
            Integer totalTestCount,
            String executionStatus,
            String errorMessage,
            LocalDateTime submittedAt,
            List<TestCaseResultView> testCaseResults
    ) {
    }

    record TestCaseResultView(
            Long testCaseId,
            Boolean passed,
            Boolean hidden,
            String actualOutput,
            String errorMessage,
            Integer executionTimeMs,
            Integer score
    ) {
    }
}
