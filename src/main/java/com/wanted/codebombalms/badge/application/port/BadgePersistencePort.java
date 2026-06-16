package com.wanted.codebombalms.badge.application.port;

import com.wanted.codebombalms.badge.domain.model.Badge;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BadgePersistencePort {

    Badge save(Badge badge);

    List<Badge> findAllNotDeleted();

    Optional<Badge> findNotDeletedById(Long badgeId);

    boolean existsNotDeletedByName(String badgeName);

    boolean existsNotDeletedByNameExcludingId(
            String badgeName,
            Long badgeId
    );

    List<Badge> findGrantableBadges(Integer totalPoint);

    List<Badge> findDeletedBefore(LocalDateTime threshold);

    void deleteById(Long badgeId);

    List<Badge> findAllNotDeletedByIds(List<Long> badgeIds);

}
