package com.wanted.codebombalms.badge.infrastructure.persistence;

import com.wanted.codebombalms.badge.application.port.BadgePersistencePort;
import com.wanted.codebombalms.badge.domain.model.Badge;
import com.wanted.codebombalms.badge.domain.model.BadgeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BadgePersistenceAdapter implements BadgePersistencePort {

    private final SpringDataBadgeRepository badgeRepository;

    @Override
    public Badge save(Badge badge) {
        return badgeRepository.save(BadgeJpaEntity.from(badge))
                .toDomain();
    }

    @Override
    public List<Badge> findAllNotDeleted() {
        return badgeRepository
                .findAllByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(BadgeJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Badge> findNotDeletedById(Long badgeId) {
        return badgeRepository.findByBadgeIdAndDeletedAtIsNull(badgeId)
                .map(BadgeJpaEntity::toDomain);
    }

    @Override
    public boolean existsNotDeletedByName(String badgeName) {
        return badgeRepository.existsByBadgeNameAndDeletedAtIsNull(
                badgeName
        );
    }

    @Override
    public boolean existsNotDeletedByNameExcludingId(
            String badgeName,
            Long badgeId
    ) {
        return badgeRepository
                .existsByBadgeNameAndBadgeIdNotAndDeletedAtIsNull(
                        badgeName,
                        badgeId
                );
    }

    @Override
    public List<Badge> findGrantableBadges(Integer totalPoint) {
        return badgeRepository
                .findAllByStatusAndDeletedAtIsNullAndRequiredPointLessThanEqualOrderByRequiredPointAsc(
                        BadgeStatus.ACTIVE,
                        totalPoint
                )
                .stream()
                .map(BadgeJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Badge> findDeletedBefore(LocalDateTime threshold) {
        return badgeRepository.findAllByDeletedAtBefore(threshold)
                .stream()
                .map(BadgeJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long badgeId) {
        badgeRepository.deleteById(badgeId);
    }

    @Override
    public List<Badge> findAllNotDeletedByIds(List<Long> badgeIds) {
        if (badgeIds.isEmpty()) {
            return List.of();
        }

        return badgeRepository
                .findAllByBadgeIdInAndDeletedAtIsNull(badgeIds)
                .stream()
                .map(BadgeJpaEntity::toDomain)
                .toList();
    }
}
