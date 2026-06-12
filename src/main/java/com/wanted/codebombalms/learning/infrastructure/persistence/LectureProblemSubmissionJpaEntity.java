package com.wanted.codebombalms.learning.infrastructure.persistence;

import com.wanted.codebombalms.learning.domain.model.LectureProblemSubmission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "lecture_problem_submission")
public class LectureProblemSubmissionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_problem_submission_id")
    private Long lectureProblemSubmissionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "lecture_problem_set_id", nullable = false)
    private Long lectureProblemSetId;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "submitted_code", nullable = false, columnDefinition = "TEXT")
    private String submittedCode;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "attempt_no", nullable = false)
    private Integer attemptNo;

    @Column(name = "passed_test_count", nullable = false)
    private Integer passedTestCount;

    @Column(name = "total_test_count", nullable = false)
    private Integer totalTestCount;

    @Column(name = "execution_status")
    private String executionStatus;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    protected LectureProblemSubmissionJpaEntity() {
    }

    public static LectureProblemSubmissionJpaEntity from(LectureProblemSubmission submission) {
        LectureProblemSubmissionJpaEntity entity = new LectureProblemSubmissionJpaEntity();
        entity.lectureProblemSubmissionId = submission.lectureProblemSubmissionId();
        entity.userId = submission.userId();
        entity.lectureProblemSetId = submission.lectureProblemSetId();
        entity.problemId = submission.problemId();
        entity.submittedCode = submission.submittedCode();
        entity.correct = submission.correct();
        entity.attemptNo = submission.attemptNo();
        entity.passedTestCount = submission.passedTestCount();
        entity.totalTestCount = submission.totalTestCount();
        entity.executionStatus = submission.executionStatus();
        entity.errorMessage = submission.errorMessage();
        entity.submittedAt = submission.submittedAt();
        return entity;
    }

    public LectureProblemSubmission toDomain() {
        return new LectureProblemSubmission(
                lectureProblemSubmissionId,
                userId,
                lectureProblemSetId,
                problemId,
                submittedCode,
                correct,
                attemptNo,
                passedTestCount,
                totalTestCount,
                executionStatus,
                errorMessage,
                submittedAt
        );
    }

    @PrePersist
    void prePersist() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }
}
