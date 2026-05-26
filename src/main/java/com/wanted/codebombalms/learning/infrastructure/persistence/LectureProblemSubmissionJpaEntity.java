package com.wanted.codebombalms.learning.infrastructure.persistence;

import com.wanted.codebombalms.learning.domain.model.LectureProblemSubmission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(name = "course_problem_step_id", nullable = false)
    private Long courseProblemStepId;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "submitted_answer", columnDefinition = "TEXT")
    private String submittedAnswer;

    @Column(name = "is_correct")
    private Boolean correct;

    @Column(name = "attempt_no")
    private Integer attemptNo;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    protected LectureProblemSubmissionJpaEntity() {
    }

    public static LectureProblemSubmissionJpaEntity from(LectureProblemSubmission submission) {
        LectureProblemSubmissionJpaEntity entity = new LectureProblemSubmissionJpaEntity();
        entity.lectureProblemSubmissionId = submission.lectureProblemSubmissionId();
        entity.userId = submission.userId();
        entity.courseProblemStepId = submission.courseProblemStepId();
        entity.problemId = submission.problemId();
        entity.submittedAnswer = submission.submittedAnswer();
        entity.correct = submission.correct();
        entity.attemptNo = submission.attemptNo();
        entity.submittedAt = submission.submittedAt();
        return entity;
    }

    public LectureProblemSubmission toDomain() {
        return new LectureProblemSubmission(
                lectureProblemSubmissionId,
                userId,
                courseProblemStepId,
                problemId,
                submittedAnswer,
                correct,
                attemptNo,
                submittedAt
        );
    }
}