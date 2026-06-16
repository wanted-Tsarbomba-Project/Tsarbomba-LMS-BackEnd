package com.wanted.codebombalms.badge.infrastructure.persistence;

import com.wanted.codebombalms.badge.domain.model.BadgeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataBadgeRepository
        extends JpaRepository<BadgeJpaEntity, Long> {

    List<BadgeJpaEntity> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

    Optional<BadgeJpaEntity> findByBadgeIdAndDeletedAtIsNull(Long badgeId);

    boolean existsByBadgeNameAndDeletedAtIsNull(String badgeName);

    boolean existsByBadgeNameAndBadgeIdNotAndDeletedAtIsNull(
            String badgeName,
            Long badgeId
    );

    List<BadgeJpaEntity>
    findAllByStatusAndDeletedAtIsNullAndRequiredPointLessThanEqualOrderByRequiredPointAsc(
            BadgeStatus status,
            Integer totalPoint
    );

    List<BadgeJpaEntity> findAllByDeletedAtBefore(LocalDateTime threshold);

    List<BadgeJpaEntity> findAllByBadgeIdInAndDeletedAtIsNull(
            List<Long> badgeIds
    );
}
