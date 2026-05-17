package com.wanted.codebombalms.domain.problems.set.entity;

import com.wanted.codebombalms.domain.problems.category.entity.ProblemCategory;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ProblemSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long problemSetId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ProblemCategory category;

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

    private LocalDateTime createdAt;

    protected ProblemSet() {
    }
    public ProblemSet(
            ProblemCategory category,
            String title,
            String description,
            String difficulty,
            Integer totalProblemCount
    ) {
        this.category = category;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.status = "ACTIVE";
        this.totalProblemCount = totalProblemCount;
        this.completedUserCount = 0;
        this.startedUserCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    public Long getProblemSetId() {
        return problemSetId;
    }

    public ProblemCategory getCategory() {
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
}
