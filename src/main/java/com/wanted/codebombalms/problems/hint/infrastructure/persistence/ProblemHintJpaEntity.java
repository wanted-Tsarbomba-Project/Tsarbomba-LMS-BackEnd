package com.wanted.codebombalms.problems.hint.infrastructure.persistence;

import com.wanted.codebombalms.problems.problem.infrastructure.persistence.ProblemJpaEntity;
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
@Table(name = "problem_hint")
public class ProblemHintJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hintId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private ProblemJpaEntity problem;

    @Column(nullable = false)
    private Integer hintOrder;

    @Column(columnDefinition = "TEXT")
    private String hintContent;

    private LocalDateTime createdAt;

    protected ProblemHintJpaEntity() {
    }

    public ProblemHintJpaEntity(ProblemJpaEntity problem, Integer hintOrder, String hintContent) {
        this.problem = problem;
        this.hintOrder = hintOrder;
        this.hintContent = hintContent;
        this.createdAt = LocalDateTime.now();
    }

    public void update(String hintContent) {
        this.hintContent = hintContent;
    }

    public Long getHintId() {
        return hintId;
    }

    public ProblemJpaEntity getProblem() {
        return problem;
    }

    public Integer getHintOrder() {
        return hintOrder;
    }

    public String getHintContent() {
        return hintContent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
