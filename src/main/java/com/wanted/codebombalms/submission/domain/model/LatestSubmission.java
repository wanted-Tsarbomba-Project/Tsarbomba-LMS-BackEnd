package com.wanted.codebombalms.submission.domain.model;

import java.time.LocalDateTime;

public record LatestSubmission(
        Long problemId,
        Integer problemNumber,
        String submittedAnswer,
        Boolean correct,
        LocalDateTime submittedAt
) {
}
