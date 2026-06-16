package com.wanted.codebombalms.learning.domain.model;

import java.time.LocalDateTime;

public class LectureProgress {

    private static final double COMPLETION_THRESHOLD = 0.9;

    private final Long lectureProgressId;
    private final Long userId;
    private final Long lectureId;
    private boolean completed;
    private LocalDateTime completedAt;
    private LocalDateTime lastWatchedAt;
    private int lastPositionSec;
    private Integer durationSec;
    private int watchedSec;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private LectureProgress(
            Long lectureProgressId,
            Long userId,
            Long lectureId,
            boolean completed,
            LocalDateTime completedAt,
            LocalDateTime lastWatchedAt,
            int lastPositionSec,
            Integer durationSec,
            int watchedSec,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.lectureProgressId = lectureProgressId;
        this.userId = userId;
        this.lectureId = lectureId;
        this.completed = completed;
        this.completedAt = completedAt;
        this.lastWatchedAt = lastWatchedAt;
        this.lastPositionSec = lastPositionSec;
        this.durationSec = durationSec;
        this.watchedSec = watchedSec;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static LectureProgress create(Long userId, Long lectureId) {
        return new LectureProgress(null, userId, lectureId, false, null, null, 0, null, 0, null, null);
    }

    public static LectureProgress restore(
            Long lectureProgressId,
            Long userId,
            Long lectureId,
            boolean completed,
            LocalDateTime completedAt,
            LocalDateTime lastWatchedAt,
            int lastPositionSec,
            Integer durationSec,
            int watchedSec,
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
                lastPositionSec,
                durationSec,
                watchedSec,
                createdAt,
                updatedAt
        );
    }

    public void recordVideoProgress(int lastPositionSec, Integer durationSec, int watchedDeltaSec) {
        this.lastPositionSec = lastPositionSec;
        if (durationSec != null) {
            this.durationSec = durationSec;
        }
        this.watchedSec = calculateWatchedSec(watchedDeltaSec);
        this.lastWatchedAt = LocalDateTime.now();

        if (!this.completed && isCompletionConditionSatisfied()) {
            markCompleted();
        }
    }

    public void complete() {
        this.lastWatchedAt = LocalDateTime.now();
        if (!this.completed) {
            markCompleted();
        }
    }

    private int calculateWatchedSec(int watchedDeltaSec) {
        int nextWatchedSec = this.watchedSec + watchedDeltaSec;
        if (durationSec == null) {
            return nextWatchedSec;
        }
        return Math.min(nextWatchedSec, durationSec);
    }

    private boolean isCompletionConditionSatisfied() {
        if (durationSec == null || durationSec <= 0) {
            return false;
        }
        int requiredSec = (int) Math.ceil(durationSec * COMPLETION_THRESHOLD);
        return lastPositionSec >= requiredSec && watchedSec >= requiredSec;
    }

    private void markCompleted() {
        this.completed = true;
        this.completedAt = LocalDateTime.now();
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

    public int getLastPositionSec() {
        return lastPositionSec;
    }

    public Integer getDurationSec() {
        return durationSec;
    }

    public int getWatchedSec() {
        return watchedSec;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
