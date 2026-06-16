package com.wanted.codebombalms.badge.application.port;

import com.wanted.codebombalms.badge.domain.model.UserBadge;

import java.util.List;
import java.util.Optional;

public interface UserBadgePersistencePort {

    UserBadge save(UserBadge userBadge);

    List<UserBadge> saveAll(List<UserBadge> userBadges);

    List<UserBadge> findAllByUserId(Long userId);

    Optional<UserBadge> findByUserIdAndBadgeId(
            Long userId,
            Long badgeId
    );

    boolean existsByUserIdAndBadgeId(
            Long userId,
            Long badgeId
    );

    List<UserBadge> findAllEquippedByUserId(Long userId);

    void deleteAllByBadgeId(Long badgeId);

    List<UserBadge> findAllEquippedByBadgeId(Long badgeId);
}
