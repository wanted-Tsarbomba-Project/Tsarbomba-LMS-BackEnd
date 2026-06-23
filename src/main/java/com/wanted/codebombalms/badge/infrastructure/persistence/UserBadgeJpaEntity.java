package com.wanted.codebombalms.badge.infrastructure.persistence;

import com.wanted.codebombalms.badge.domain.model.UserBadge;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_badge",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_badge_user_badge",
                        columnNames = {"user_id", "badge_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_user_badge_user_equipped",
                        columnList = "user_id, is_equipped"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBadgeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_badge_id")
    private Long userBadgeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "badge_id", nullable = false)
    private Long badgeId;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    @Column(name = "is_equipped", nullable = false)
    private boolean equipped;

    public static UserBadgeJpaEntity from(UserBadge userBadge) {
        UserBadgeJpaEntity entity = new UserBadgeJpaEntity();
        entity.apply(userBadge);
        return entity;
    }

    public void apply(UserBadge userBadge) {
        this.userBadgeId = userBadge.getUserBadgeId();
        this.userId = userBadge.getUserId();
        this.badgeId = userBadge.getBadgeId();
        this.earnedAt = userBadge.getEarnedAt();
        this.equipped = userBadge.isEquipped();
    }

    public UserBadge toDomain() {
        return UserBadge.restore(
                userBadgeId,
                userId,
                badgeId,
                earnedAt,
                equipped
        );
    }
}
