package com.wanted.codebombalms.submission.domain.model;

import java.time.LocalDateTime;

public record LatestSubmission(
        Long submissionId,
        Long problemId,
        Integer problemOrder,
        String submittedCode,
        Boolean correct,
        LocalDateTime submittedAt
) {
}
