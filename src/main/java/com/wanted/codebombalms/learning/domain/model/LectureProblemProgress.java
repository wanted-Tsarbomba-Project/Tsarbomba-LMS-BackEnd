package com.wanted.codebombalms.learning.domain.model;

import java.time.LocalDateTime;

public class LectureProblemProgress {

    private final Long lectureProblemProgressId;
    private final Long userId;
    private final Long lectureProblemSetId;
    private Integer currentProblemNumber;
    private boolean completed;
    private LocalDateTime completedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private LectureProblemProgress(
            Long lectureProblemProgressId,
            Long userId,
            Long lectureProblemSetId,
            Integer currentProblemNumber,
            boolean completed,
            LocalDateTime completedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.lectureProblemProgressId = lectureProblemProgressId;
        this.userId = userId;
        this.lectureProblemSetId = lectureProblemSetId;
        this.currentProblemNumber = currentProblemNumber;
        this.completed = completed;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static LectureProblemProgress create(Long userId, Long lectureProblemSetId) {
        return new LectureProblemProgress(null, userId, lectureProblemSetId, 1, false, null, null, null);
    }

    public static LectureProblemProgress restore(
            Long lectureProblemProgressId,
            Long userId,
            Long lectureProblemSetId,
            Integer currentProblemNumber,
            boolean completed,
            LocalDateTime completedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new LectureProblemProgress(
                lectureProblemProgressId,
                userId,
                lectureProblemSetId,
                currentProblemNumber,
                completed,
                completedAt,
                createdAt,
                updatedAt
        );
    }

    public void complete() {
        this.completed = true;
        this.completedAt = LocalDateTime.now();
    }

    public void updateProgress(Integer currentProblemNumber, boolean completed) {
        if (currentProblemNumber != null) {
            this.currentProblemNumber = currentProblemNumber;
        }
        if (completed) {
            complete();
        }
    }

    public void completeAt(Integer currentProblemNumber) {
        if (currentProblemNumber != null) {
            this.currentProblemNumber = currentProblemNumber;
        }
        complete();
    }

    public Long getLectureProblemProgressId() {
        return lectureProblemProgressId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getLectureProblemSetId() {
        return lectureProblemSetId;
    }

    public Integer getCurrentProblemNumber() {
        return currentProblemNumber;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
