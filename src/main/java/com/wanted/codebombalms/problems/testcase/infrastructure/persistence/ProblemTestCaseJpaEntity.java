package com.wanted.codebombalms.problems.testcase.infrastructure.persistence;

import com.wanted.codebombalms.problems.problem.infrastructure.persistence.ProblemJpaEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "problem_test_case",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_problem_test_case_problem_order",
                        columnNames = {"problem_id", "test_order"}
                )
        }
)
public class ProblemTestCaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testCaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private ProblemJpaEntity problem;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String testCode;

    @Column(nullable = false)
    private Integer testOrder;

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
            Integer testOrder,
            Boolean hidden,
            Integer timeoutMs
    ) {
        ProblemTestCaseJpaEntity testCase = new ProblemTestCaseJpaEntity();
        testCase.problem = problem;
        testCase.testCode = testCode;
        testCase.testOrder = testOrder;
        testCase.hidden = hidden;
        testCase.timeoutMs = timeoutMs;
        testCase.status = "ACTIVE";
        testCase.createdAt = LocalDateTime.now();
        testCase.updatedAt = testCase.createdAt;
        return testCase;
    }

    public void update(
            String testCode,
            Integer testOrder,
            Boolean hidden,
            Integer timeoutMs
    ) {
        this.testCode = testCode;
        this.testOrder = testOrder;
        this.hidden = hidden;
        this.timeoutMs = timeoutMs;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = "INACTIVE";
        this.updatedAt = LocalDateTime.now();
    }

    public void moveToOrder(Integer testOrder) {
        this.testOrder = testOrder;
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

    public Integer getTestOrder() {
        return testOrder;
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
