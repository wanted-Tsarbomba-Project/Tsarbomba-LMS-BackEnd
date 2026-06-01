package com.wanted.codebombalms.reward.point.infrastructure.persistence;

import com.wanted.codebombalms.reward.point.domain.model.PointHistory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "point_history",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_point_history_user_problem",
                        columnNames = {"user_id", "problem_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PointHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_history_id")
    private Long pointHistoryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "submission_id", nullable = false)
    private Long submissionId;

    @Column(name = "point", nullable = false)
    private Integer point;

    @Column(name = "reason")
    private String reason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static PointHistoryJpaEntity from(PointHistory history) {
        PointHistoryJpaEntity entity = new PointHistoryJpaEntity();
        entity.pointHistoryId = history.pointHistoryId();
        entity.userId = history.userId();
        entity.problemId = history.problemId();
        entity.submissionId = history.submissionId();
        entity.point = history.point();
        entity.reason = history.reason();
        return entity;
    }

    public PointHistory toDomain() {
        return new PointHistory(
                pointHistoryId,
                userId,
                problemId,
                submissionId,
                point,
                reason,
                createdAt
        );
    }
}
