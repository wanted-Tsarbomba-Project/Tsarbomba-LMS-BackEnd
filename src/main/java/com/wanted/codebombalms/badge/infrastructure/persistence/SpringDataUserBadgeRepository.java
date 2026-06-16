package com.wanted.codebombalms.badge.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SpringDataUserBadgeRepository
        extends JpaRepository<UserBadgeJpaEntity, Long> {

    List<UserBadgeJpaEntity> findAllByUserIdOrderByEarnedAtDesc(
            Long userId
    );

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
