package com.wanted.codebombalms.submission.infrastructure.persistence;

import com.wanted.codebombalms.problems.testcase.infrastructure.persistence.ProblemTestCaseJpaEntity;
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
@Table(name = "submission_test_result")
public class SubmissionTestResultJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long submissionTestResultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private SubmissionJpaEntity submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id", nullable = false)
    private ProblemTestCaseJpaEntity testCase;

    @Column(name = "is_passed", nullable = false)
    private Boolean passed;

    @Column(columnDefinition = "TEXT")
    private String actualOutput;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Integer executionTimeMs;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected SubmissionTestResultJpaEntity() {
    }

    public SubmissionTestResultJpaEntity(
            SubmissionJpaEntity submission,
            ProblemTestCaseJpaEntity testCase,
            Boolean passed,
            String actualOutput,
            String errorMessage,
            Integer executionTimeMs
    ) {
        this.submission = submission;
        this.testCase = testCase;
        this.passed = passed;
        this.actualOutput = actualOutput;
        this.errorMessage = errorMessage;
        this.executionTimeMs = executionTimeMs;
        this.createdAt = LocalDateTime.now();
    }

    public Long getSubmissionTestResultId() {
        return submissionTestResultId;
    }

    public SubmissionJpaEntity getSubmission() {
        return submission;
    }

    public ProblemTestCaseJpaEntity getTestCase() {
        return testCase;
    }

    public Boolean getPassed() {
        return passed;
    }

    public String getActualOutput() {
        return actualOutput;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Integer getExecutionTimeMs() {
        return executionTimeMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}