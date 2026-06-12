package com.wanted.codebombalms.reward.point.infrastructure.persistence;

import com.wanted.codebombalms.reward.point.domain.model.PointRewardTask;
import com.wanted.codebombalms.reward.point.domain.model.PointRewardTaskStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "point_reward_task",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_point_reward_task_user_problem",
                        columnNames = {"user_id", "problem_id"}
                ),
                @UniqueConstraint(
                        name = "uk_point_reward_task_submission",
                        columnNames = "submission_id"
                )
        },
        indexes = @Index(
                name = "idx_point_reward_task_recovery",
                columnList = "status, next_retry_at, created_at"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PointRewardTaskJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_reward_task_id")
    private Long taskId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "submission_id", nullable = false)
    private Long submissionId;

    @Column(nullable = false)
    private Integer point;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointRewardTaskStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "last_error_message", length = 500)
    private String lastErrorMessage;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static PointRewardTaskJpaEntity from(PointRewardTask task) {
        PointRewardTaskJpaEntity entity = new PointRewardTaskJpaEntity();
        entity.taskId = task.taskId();
        entity.userId = task.userId();
        entity.problemId = task.problemId();
        entity.submissionId = task.submissionId();
        entity.point = task.point();
        entity.status = task.status();
        entity.retryCount = task.retryCount();
        entity.lastErrorMessage = task.lastErrorMessage();
        entity.nextRetryAt = task.nextRetryAt();
        entity.createdAt = task.createdAt();
        entity.updatedAt = task.updatedAt();
        return entity;
    }

    public PointRewardTask toDomain() {
        return new PointRewardTask(
                taskId,
                userId,
                problemId,
                submissionId,
                point,
                status,
                retryCount,
                lastErrorMessage,
                nextRetryAt,
                createdAt,
                updatedAt
        );
    }
}
