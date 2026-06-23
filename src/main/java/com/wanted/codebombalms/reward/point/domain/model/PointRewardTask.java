package com.wanted.codebombalms.reward.point.domain.model;

import java.time.LocalDateTime;

public record PointRewardTask(
        Long taskId,
        Long userId,
        Long problemId,
        Long submissionId,
        Integer point,
        PointRewardTaskStatus status,
        Integer retryCount,
        String lastErrorMessage,
        LocalDateTime nextRetryAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    private static final int MAX_ERROR_MESSAGE_LENGTH = 500;

    public static PointRewardTask create(
            Long userId,
            Long problemId,
            Long submissionId,
            Integer point,
            LocalDateTime now
    ) {
        return new PointRewardTask(
                null,
                userId,
                problemId,
                submissionId,
                point,
                PointRewardTaskStatus.PENDING,
                0,
                null,
                now,
                null,
                null
        );
    }

    public PointRewardTask failPermanently(String errorMessage) {
        return update(
                PointRewardTaskStatus.FAILED,
                retryCount,
                truncate(errorMessage),
                null
        );
    }

    public PointRewardTask complete() {
        return update(
                PointRewardTaskStatus.COMPLETED,
                retryCount,
                null,
                null
        );
    }

    public PointRewardTask retry(
            String errorMessage,
            LocalDateTime nextRetryAt,
            int maxRetryCount
    ) {
        int nextRetryCount = retryCount + 1;

        PointRewardTaskStatus nextStatus =
                nextRetryCount >= maxRetryCount
                        ? PointRewardTaskStatus.FAILED
                        : PointRewardTaskStatus.PENDING;

        return update(
                nextStatus,
                nextRetryCount,
                truncate(errorMessage),
                nextRetryAt
        );
    }

    private PointRewardTask update(
            PointRewardTaskStatus status,
            Integer retryCount,
            String errorMessage,
            LocalDateTime nextRetryAt
    ) {
        return new PointRewardTask(
                taskId,
                userId,
                problemId,
                submissionId,
                point,
                status,
                retryCount,
                errorMessage,
                nextRetryAt,
                createdAt,
                updatedAt
        );
    }

    private String truncate(String message) {
        if (message == null || message.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return message;
        }

        return message.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}
