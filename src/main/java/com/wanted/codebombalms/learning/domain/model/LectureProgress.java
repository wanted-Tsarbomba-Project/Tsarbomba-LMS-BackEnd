package com.wanted.codebombalms.learning.domain.model;

import java.time.LocalDateTime;

public class LectureProgress {

    private final Long lectureProgressId;
    private final Long userId;
    private final Long lectureId;
    private boolean completed;
    private LocalDateTime completedAt;
    private LocalDateTime lastWatchedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private LectureProgress(
            Long lectureProgressId,
            Long userId,
            Long lectureId,
            boolean completed,
            LocalDateTime completedAt,
            LocalDateTime lastWatchedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.lectureProgressId = lectureProgressId;
        this.userId = userId;
        this.lectureId = lectureId;
        this.completed = completed;
        this.completedAt = completedAt;
        this.lastWatchedAt = lastWatchedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static LectureProgress create(Long userId, Long lectureId) {
        return new LectureProgress(null, userId, lectureId, false, null, null, null, null);
    }

    public static LectureProgress restore(
            Long lectureProgressId,
            Long userId,
            Long lectureId,
            boolean completed,
            LocalDateTime completedAt,
            LocalDateTime lastWatchedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new LectureProgress(
                lectureProgressId,
                userId,
                lectureId,
                completed,
                completedAt,
                lastWatchedAt,
                createdAt,
                updatedAt
        );
    }

    public void record(boolean completed) {
        this.lastWatchedAt = LocalDateTime.now();
        if (completed && !this.completed) {
            this.completed = true;
            this.completedAt = LocalDateTime.now();
        }
    }

    public Long getLectureProgressId() {
        return lectureProgressId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getLectureId() {
        return lectureId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getLastWatchedAt() {
        return lastWatchedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
