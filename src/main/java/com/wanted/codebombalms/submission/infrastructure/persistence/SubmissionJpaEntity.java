package com.wanted.codebombalms.submission.infrastructure.persistence;

import com.wanted.codebombalms.problems.problem.infrastructure.persistence.ProblemJpaEntity;
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
@Table(name = "submission")
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
    private String submittedAnswer;

    private Boolean isCorrect;

    @Column(nullable = false)
    private Integer earnedScore;

    @Column(nullable = false)
    private Integer attemptNo;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    protected SubmissionJpaEntity() {
    }

    public SubmissionJpaEntity(
            Long userId,
            ProblemJpaEntity problem,
            String submittedAnswer,
            Boolean isCorrect,
            Integer earnedScore,
            Integer attemptNo
    ) {
        this.userId = userId;
        this.problem = problem;
        this.submittedAnswer = submittedAnswer;
        this.isCorrect = isCorrect;
        this.earnedScore = earnedScore;
        this.attemptNo = attemptNo;
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

    public String getSubmittedAnswer() {
        return submittedAnswer;
    }

    public Boolean getCorrect() {
        return isCorrect;
    }

    public Integer getEarnedScore() {
        return earnedScore;
    }

    public Integer getAttemptNo() {
        return attemptNo;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}
