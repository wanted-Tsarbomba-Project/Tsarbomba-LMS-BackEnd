package com.wanted.codebombalms.problems.explanation.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "problem_explanation_view",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_problem_explanation_view_user_problem",
                        columnNames = {"user_id", "problem_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_problem_explanation_view_user_problem_set",
                        columnList = "user_id, problem_set_id"
                )
        }
)
public class ProblemExplanationViewJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "explanation_view_id")
    private Long explanationViewId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "problem_set_id", nullable = false)
    private Long problemSetId;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    protected ProblemExplanationViewJpaEntity() {
    }

    public ProblemExplanationViewJpaEntity(
            Long userId,
            Long problemId,
            Long problemSetId
    ) {
        this.userId = userId;
        this.problemId = problemId;
        this.problemSetId = problemSetId;
        this.viewedAt = LocalDateTime.now();
    }

    public Long getProblemId() {
        return problemId;
    }
}
