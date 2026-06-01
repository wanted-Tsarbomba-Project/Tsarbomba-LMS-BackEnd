package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import com.wanted.codebombalms.problems.category.infrastructure.persistence.ProblemCategoryJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "problem_set")
public class ProblemSetJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long problemSetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProblemCategoryJpaEntity category;

    @Column(nullable = false)
    private String title;

    private String description;

    private String difficulty;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Integer totalProblemCount;

    @Column(nullable = false)
    private Integer completedUserCount;

    @Column(nullable = false)
    private Integer startedUserCount;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    protected ProblemSetJpaEntity() {
    }

    public ProblemSetJpaEntity(
            ProblemCategoryJpaEntity category,
            String title,
            String description,
            String difficulty,
            Integer totalProblemCount,
            Long createdBy
    ) {
        this.category = category;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.status = "ACTIVE";
        this.totalProblemCount = totalProblemCount;
        this.completedUserCount = 0;
        this.startedUserCount = 0;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public void update(
            ProblemCategoryJpaEntity category,
            String title,
            String description,
            String difficulty
    ) {
        this.category = category;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
    }

    public void updateTotalProblemCount(Integer totalProblemCount) {
        this.totalProblemCount = totalProblemCount;
    }

    public void deactivate() {
        this.status = "INACTIVE";
        this.deletedAt = LocalDateTime.now();
    }

    public Long getProblemSetId() {
        return problemSetId;
    }

    public ProblemCategoryJpaEntity getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Integer getTotalProblemCount() {
        return totalProblemCount;
    }

    public Integer getCompletedUserCount() {
        return completedUserCount;
    }

    public Integer getStartedUserCount() {
        return startedUserCount;
    }

    public void increaseStartedUserCount() {
        this.startedUserCount = this.startedUserCount + 1;
    }

    public void increaseCompletedUserCount() {
        this.completedUserCount = this.completedUserCount + 1;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
