package com.wanted.codebombalms.problems.testcase.infrastructure.persistence;

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
@Table(name = "problem_test_case")
public class ProblemTestCaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testCaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private ProblemJpaEntity problem;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String testCode;

    @Column(columnDefinition = "TEXT")
    private String expectedResult;

    @Column(nullable = false)
    private Integer testOrder;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "is_hidden", nullable = false)
    private Boolean hidden;

    @Column(nullable = false)
    private Integer timeoutMs;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected ProblemTestCaseJpaEntity() {
    }

    public static ProblemTestCaseJpaEntity create(
            ProblemJpaEntity problem,
            String testCode,
            String expectedResult,
            Integer testOrder,
            Integer score,
            Boolean hidden,
            Integer timeoutMs
    ) {
        ProblemTestCaseJpaEntity testCase = new ProblemTestCaseJpaEntity();
        testCase.problem = problem;
        testCase.testCode = testCode;
        testCase.expectedResult = expectedResult;
        testCase.testOrder = testOrder;
        testCase.score = score;
        testCase.hidden = hidden;
        testCase.timeoutMs = timeoutMs;
        testCase.status = "ACTIVE";
        testCase.createdAt = LocalDateTime.now();
        testCase.updatedAt = testCase.createdAt;
        return testCase;
    }

    public void update(
            String testCode,
            String expectedResult,
            Integer testOrder,
            Integer score,
            Boolean hidden,
            Integer timeoutMs
    ) {
        this.testCode = testCode;
        this.expectedResult = expectedResult;
        this.testOrder = testOrder;
        this.score = score;
        this.hidden = hidden;
        this.timeoutMs = timeoutMs;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = "INACTIVE";
        this.updatedAt = LocalDateTime.now();
    }

    public Long getTestCaseId() {
        return testCaseId;
    }

    public ProblemJpaEntity getProblem() {
        return problem;
    }

    public String getTestCode() {
        return testCode;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public Integer getTestOrder() {
        return testOrder;
    }

    public Integer getScore() {
        return score;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
