package com.wanted.codebombalms.reward.point.domain.model;

import java.time.LocalDateTime;

public record PointHistory(
        Long pointHistoryId,
        Long userId,
        Long problemId,
        Long submissionId,
        Integer point,
        String reason,
        LocalDateTime createdAt
) {
    public static PointHistory create(
            Long userId,
            Long problemId,
            Long submissionId,
            Integer point,
            String reason
    ) {
        return new PointHistory(null, userId, problemId, submissionId, point, reason, null);
    }
}
