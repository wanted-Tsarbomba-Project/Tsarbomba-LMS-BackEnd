package com.wanted.codebombalms.learning.infrastructure.persistence;

import com.wanted.codebombalms.learning.domain.model.LectureProblemProgress;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "lecture_problem_progress")
public class LectureProblemProgressJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_problem_progress_id")
    private Long lectureProblemProgressId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_problem_step_id", nullable = false)
    private Long courseProblemStepId;

    @Column(name = "current_problem_number", nullable = false)
    private Integer currentProblemNumber;

    @Column(name = "is_completed", nullable = false)
    private boolean completed;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected LectureProblemProgressJpaEntity() {
    }

    public static LectureProblemProgressJpaEntity from(LectureProblemProgress progress) {
        LectureProblemProgressJpaEntity entity = new LectureProblemProgressJpaEntity();
        entity.lectureProblemProgressId = progress.getLectureProblemProgressId();
        entity.userId = progress.getUserId();
        entity.courseProblemStepId = progress.getCourseProblemStepId();
        entity.currentProblemNumber = progress.getCurrentProblemNumber();
        entity.completed = progress.isCompleted();
        entity.completedAt = progress.getCompletedAt();
        entity.createdAt = progress.getCreatedAt();
        entity.updatedAt = progress.getUpdatedAt();
        return entity;
    }

    public void apply(LectureProblemProgress progress) {
        this.currentProblemNumber = progress.getCurrentProblemNumber();
        this.completed = progress.isCompleted();
        this.completedAt = progress.getCompletedAt();
    }

    public LectureProblemProgress toDomain() {
        return LectureProblemProgress.restore(
                lectureProblemProgressId,
                userId,
                courseProblemStepId,
                currentProblemNumber,
                completed,
                completedAt,
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
