package com.wanted.codebombalms.submission.domain.model;

import java.time.LocalDateTime;

public record LatestSubmission(
        Long problemId,
        Integer problemOrder,
        String submittedAnswer,
        Boolean correct,
        LocalDateTime submittedAt
) {
}
