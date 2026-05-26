package com.wanted.codebombalms.submission.domain.model;

import java.time.LocalDateTime;

public record CodeSubmissionListItem(
        Long submissionId,
        Long problemId,
        Boolean correct,
        Integer passedTestCount,
        Integer totalTestCount,
        String executionStatus,
        LocalDateTime submittedAt
) {
}