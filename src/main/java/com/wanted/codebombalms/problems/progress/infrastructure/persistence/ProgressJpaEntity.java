package com.wanted.codebombalms.problems.progress.infrastructure.persistence;

import com.wanted.codebombalms.problems.set.infrastructure.persistence.ProblemSetJpaEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "problem_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_problem_progress_user_problem_set",
                        columnNames = {"user_id", "problem_set_id"}
                )
        }
)
public class ProgressJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long progressId;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_set_id")
    private ProblemSetJpaEntity problemSet;

    @Column(nullable = false)
    private Integer currentProblemNumber;

    @Column(nullable = false)
    private Boolean isCompleted;

    private LocalDateTime completedAt;

    private LocalDateTime updatedAt;

    protected ProgressJpaEntity() {
    }

    public ProgressJpaEntity(Long userId, ProblemSetJpaEntity problemSet) {
        this.userId = userId;
        this.problemSet = problemSet;
        this.currentProblemNumber = 1;
        this.isCompleted = false;
        this.completedAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getProgressId() {
        return progressId;
    }

    public Long getUserId() {
        return userId;
    }

    public ProblemSetJpaEntity getProblemSet() {
        return problemSet;
    }

    public Integer getCurrentProblemNumber() {
        return currentProblemNumber;
    }

    public Boolean getCompleted() {
        return isCompleted;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void openNextProblem() {
        this.currentProblemNumber = this.currentProblemNumber + 1;
        this.isCompleted = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
