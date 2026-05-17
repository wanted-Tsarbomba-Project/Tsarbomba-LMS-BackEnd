package com.wanted.codebombalms.domain.problems.hint.entity;

import com.wanted.codebombalms.domain.problems.problem.entitiy.Problem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "problem_hint")
public class ProblemHint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hintId;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @Column(nullable = false)
    private Integer hintOrder;

    @Column(columnDefinition = "TEXT")
    private String hintContent;

    private LocalDateTime createdAt;

    protected ProblemHint() {
    }

    public ProblemHint(Problem problem, Integer hintOrder, String hintContent) {
        this.problem = problem;
        this.hintOrder = hintOrder;
        this.hintContent = hintContent;
        this.createdAt = LocalDateTime.now();
    }

    public Long getHintId() {
        return hintId;
    }

    public Problem getProblem() {
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
