package com.wanted.codebombalms.learning.infrastructure.persistence;

import com.wanted.codebombalms.learning.domain.model.LectureProgress;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "lecture_progress")
public class LectureProgressJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_progress_id")
    private Long lectureProgressId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "lecture_id", nullable = false)
    private Long lectureId;

    @Column(name = "is_completed", nullable = false)
    private boolean completed;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_watched_at")
    private LocalDateTime lastWatchedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected LectureProgressJpaEntity() {
    }

    public static LectureProgressJpaEntity from(LectureProgress progress) {
        LectureProgressJpaEntity entity = new LectureProgressJpaEntity();
        entity.lectureProgressId = progress.getLectureProgressId();
        entity.userId = progress.getUserId();
        entity.lectureId = progress.getLectureId();
        entity.completed = progress.isCompleted();
        entity.completedAt = progress.getCompletedAt();
        entity.lastWatchedAt = progress.getLastWatchedAt();
        entity.createdAt = progress.getCreatedAt();
        entity.updatedAt = progress.getUpdatedAt();
        return entity;
    }

    public void apply(LectureProgress progress) {
        this.completed = progress.isCompleted();
        this.completedAt = progress.getCompletedAt();
        this.lastWatchedAt = progress.getLastWatchedAt();
    }

    public LectureProgress toDomain() {
        return LectureProgress.restore(
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

    @jakarta.persistence.PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @jakarta.persistence.PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
