package com.wanted.codebombalms.badge.infrastructure.persistence;

import com.wanted.codebombalms.badge.application.port.UserBadgePersistencePort;
import com.wanted.codebombalms.badge.domain.model.UserBadge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserBadgePersistenceAdapter
        implements UserBadgePersistencePort {

    private final SpringDataUserBadgeRepository userBadgeRepository;

    @Override
    public UserBadge save(UserBadge userBadge) {
        return userBadgeRepository
                .save(UserBadgeJpaEntity.from(userBadge))
                .toDomain();
    }

    @Override
    public List<UserBadge> saveAll(List<UserBadge> userBadges) {
        var entities = userBadges.stream()
                .map(UserBadgeJpaEntity::from)
                .toList();

        return userBadgeRepository.saveAll(entities)
                .stream()
                .map(UserBadgeJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<UserBadge> findAllByUserId(Long userId) {
        return userBadgeRepository
                .findAllByUserIdOrderByEarnedAtDesc(userId)
                .stream()
                .map(UserBadgeJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<UserBadge> findByUserIdAndBadgeId(
            Long userId,
            Long badgeId
    ) {
        return userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId)
                .map(UserBadgeJpaEntity::toDomain);
    }

    @Override
    public boolean existsByUserIdAndBadgeId(
            Long userId,
            Long badgeId
    ) {
        return userBadgeRepository.existsByUserIdAndBadgeId(
                userId,
                badgeId
        );
    }

    @Override
    public List<UserBadge> findAllEquippedByUserId(Long userId) {
        return userBadgeRepository
                .findAllByUserIdAndEquippedTrue(userId)
                .stream()
                .map(UserBadgeJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<UserBadge> findAllEquippedByBadgeId(Long badgeId) {
        return userBadgeRepository
                .findAllByBadgeIdAndEquippedTrue(badgeId)
                .stream()
                .map(UserBadgeJpaEntity::toDomain)
                .toList();
    }


    @Override
    public void deleteAllByBadgeId(Long badgeId) {
        userBadgeRepository.deleteAllByBadgeId(badgeId);
    }
}
