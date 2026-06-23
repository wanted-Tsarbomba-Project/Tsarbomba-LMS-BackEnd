package com.wanted.codebombalms.badge.infrastructure.persistence;

import com.wanted.codebombalms.badge.application.query.MyBadgeRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataUserBadgeRepository
        extends JpaRepository<UserBadgeJpaEntity, Long> {

    List<UserBadgeJpaEntity> findAllByUserIdOrderByEarnedAtDesc(
            Long userId
    );

    @Query("""
            select new com.wanted.codebombalms.badge.application.query.MyBadgeRow(
                badge.badgeId,
                badge.badgeName,
                badge.description,
                badge.requiredPoint,
                badge.objectName,
                badge.status,
                userBadge.earnedAt,
                userBadge.equipped
            )
            from UserBadgeJpaEntity userBadge
            join BadgeJpaEntity badge
              on badge.badgeId = userBadge.badgeId
            where userBadge.userId = :userId
              and badge.deletedAt is null
            order by userBadge.earnedAt desc
            """)
    List<MyBadgeRow> findMyBadgeRows(@Param("userId") Long userId);

    Optional<UserBadgeJpaEntity> findByUserIdAndBadgeId(
            Long userId,
            Long badgeId
    );

    boolean existsByUserIdAndBadgeId(
            Long userId,
            Long badgeId
    );

    List<UserBadgeJpaEntity> findAllByUserIdAndEquippedTrue(
            Long userId
    );

    List<UserBadgeJpaEntity> findAllByBadgeIdAndEquippedTrue(Long badgeId);

    void deleteAllByBadgeId(Long badgeId);
}
