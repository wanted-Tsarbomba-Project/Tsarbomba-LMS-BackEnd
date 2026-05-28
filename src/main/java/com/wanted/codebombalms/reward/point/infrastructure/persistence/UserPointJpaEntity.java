package com.wanted.codebombalms.reward.point.infrastructure.persistence;

import com.wanted.codebombalms.reward.point.domain.model.UserPoint;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_point")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserPointJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_point_id")
    private Long userPointId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "total_point", nullable = false)
    private Integer totalPoint;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static UserPointJpaEntity from(UserPoint userPoint) {
        UserPointJpaEntity entity = new UserPointJpaEntity();
        entity.userPointId = userPoint.userPointId();
        entity.userId = userPoint.userId();
        entity.totalPoint = userPoint.totalPoint();
        return entity;
    }

    public UserPoint toDomain() {
        return new UserPoint(
                userPointId,
                userId,
                totalPoint,
                createdAt,
                updatedAt
        );
    }
}
