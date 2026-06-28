package com.wanted.codebombalms.submission.infrastructure.persistence;

import com.wanted.codebombalms.problems.problem.infrastructure.persistence.ProblemJpaEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "submission",
        indexes = {
                @Index(
                        name = "idx_submission_user_problem_submitted_id",
                        columnList = "user_id, problem_id, submitted_at, submission_id"
                )
        }
)
public class SubmissionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long submissionId;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "problem_id")
    private ProblemJpaEntity problem;

    @Column(columnDefinition = "TEXT")
    private String submittedCode;

    private Boolean isCorrect;

    @Column(nullable = false)
    private Integer attemptNo;

    @Column(nullable = false)
    private Integer passedTestCount;

    @Column(nullable = false)
    private Integer totalTestCount;

    private String executionStatus;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    protected SubmissionJpaEntity() {
    }

    public SubmissionJpaEntity(
            Long userId,
            ProblemJpaEntity problem,
            String submittedCode,
            Boolean isCorrect,
            Integer attemptNo
    ) {
        this.userId = userId;
        this.problem = problem;
        this.submittedCode = submittedCode;
        this.isCorrect = isCorrect;
        this.attemptNo = attemptNo;
        this.passedTestCount = 0;
        this.totalTestCount = 0;
        this.executionStatus = null;
        this.errorMessage = null;
        this.submittedAt = LocalDateTime.now();
    }

    public SubmissionJpaEntity(
            Long userId,
            ProblemJpaEntity problem,
            String submittedCode,
            Boolean isCorrect,
            Integer attemptNo,
            Integer passedTestCount,
            Integer totalTestCount,
            String executionStatus,
            String errorMessage
    ) {
        this.userId = userId;
        this.problem = problem;
        this.submittedCode = submittedCode;
        this.isCorrect = isCorrect;
        this.attemptNo = attemptNo;
        this.passedTestCount = passedTestCount;
        this.totalTestCount = totalTestCount;
        this.executionStatus = executionStatus;
        this.errorMessage = errorMessage;
        this.submittedAt = LocalDateTime.now();
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public Long getUserId() {
        return userId;
    }

    public ProblemJpaEntity getProblem() {
        return problem;
    }

    public String getSubmittedCode() {
        return submittedCode;
    }

    public Boolean getCorrect() {
        return isCorrect;
    }

    public Integer getAttemptNo() {
        return attemptNo;
    }

    public Integer getPassedTestCount() {
        return passedTestCount;
    }

    public Integer getTotalTestCount() {
        return totalTestCount;
    }

    public String getExecutionStatus() {
        return executionStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}
