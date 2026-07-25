package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import java.time.LocalDateTime;

public record ProblemSetSummaryRow(
        Long problemSetId,
        Integer problemNumber,
        String title,
        String description,
        String difficulty,
        Integer completedUserCount,
        Integer startedUserCount,
        String completionStatus,
        LocalDateTime createdAt
) {
}
