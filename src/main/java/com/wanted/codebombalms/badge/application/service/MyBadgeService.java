package com.wanted.codebombalms.badge.application.service;

import com.wanted.codebombalms.badge.application.port.BadgeImageStoragePort;
import com.wanted.codebombalms.badge.application.port.BadgePersistencePort;
import com.wanted.codebombalms.badge.application.port.LoadUserTotalPointPort;
import com.wanted.codebombalms.badge.application.port.UserBadgePersistencePort;
import com.wanted.codebombalms.badge.application.query.BadgeSyncResult;
import com.wanted.codebombalms.badge.application.query.MyBadgeResult;
import com.wanted.codebombalms.badge.application.usecase.MyBadgeUseCase;
import com.wanted.codebombalms.badge.application.usecase.SyncUserBadgesUseCase;
import com.wanted.codebombalms.badge.domain.model.Badge;
import com.wanted.codebombalms.badge.domain.model.UserBadge;
import com.wanted.codebombalms.badge.exception.BadgeErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MyBadgeService implements MyBadgeUseCase , SyncUserBadgesUseCase {

    private final UserBadgePersistencePort userBadgePersistencePort;
    private final BadgePersistencePort badgePersistencePort;
    private final BadgeImageStoragePort badgeImageStoragePort;
    private final LoadUserTotalPointPort loadUserTotalPointPort;

    @Override
    @Transactional(readOnly = true)
    public List<MyBadgeResult> getMyBadges(Long userId) {
        List<UserBadge> userBadges =
                userBadgePersistencePort.findAllByUserId(userId);

        List<Long> badgeIds = userBadges.stream()
                .map(UserBadge::getBadgeId)
                .toList();

        Map<Long, Badge> badgesById = badgePersistencePort
                .findAllNotDeletedByIds(badgeIds)
                .stream()
                .collect(Collectors.toMap(
                        Badge::getBadgeId,
                        Function.identity()
                ));

        return userBadges.stream()
                .filter(userBadge -> badgesById.containsKey(
                        userBadge.getBadgeId()
                ))
                .map(userBadge -> toMyBadgeResult(
                        userBadge,
                        badgesById.get(userBadge.getBadgeId())
                ))
                .toList();
    }

    private MyBadgeResult toMyBadgeResult(
            UserBadge userBadge,
            Badge badge
    ) {
        return new MyBadgeResult(
                badge.getBadgeId(),
                badge.getBadgeName(),
                badge.getDescription(),
                badge.getRequiredPoint(),
                generateImageUrl(badge.getObjectName()),
                badge.getStatus().name(),
                userBadge.getEarnedAt(),
                userBadge.isEquipped()
        );
    }

    private String generateImageUrl(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return null;
        }

        return badgeImageStoragePort.generateAccessUrl(objectName);
    }

    @Override
    public MyBadgeResult equipBadge(Long userId, Long badgeId) {
        Badge badge = badgePersistencePort.findNotDeletedById(badgeId)
                .orElseThrow(() -> new NotFoundException(
                        BadgeErrorCode.BADGE_NOT_FOUND
                ));

        UserBadge selectedBadge = userBadgePersistencePort
                .findByUserIdAndBadgeId(userId, badgeId)
                .orElseThrow(() -> new NotFoundException(
                        BadgeErrorCode.USER_BADGE_NOT_FOUND
                ));

        if (selectedBadge.isEquipped()) {
            return toMyBadgeResult(selectedBadge, badge);
        }

        List<UserBadge> equippedBadges =
                userBadgePersistencePort.findAllEquippedByUserId(userId);

        equippedBadges.forEach(UserBadge::unequip);
        selectedBadge.equip();

        List<UserBadge> badgesToSave = new ArrayList<>(equippedBadges);

        badgesToSave.removeIf(userBadge ->
                userBadge.getBadgeId().equals(badgeId)
        );

        badgesToSave.add(selectedBadge);
        userBadgePersistencePort.saveAll(badgesToSave);

        return toMyBadgeResult(selectedBadge, badge);
    }

    @Override
    public BadgeSyncResult syncBadges(Long userId) {
        int totalPoint = loadUserTotalPointPort.loadTotalPoint(userId);
        return sync(userId, totalPoint);
    }

    @Override
    public BadgeSyncResult sync(Long userId, int totalPoint) {
        List<Badge> grantableBadges =
                badgePersistencePort.findGrantableBadges(totalPoint);

        Set<Long> earnedBadgeIds = userBadgePersistencePort
                .findAllByUserId(userId)
                .stream()
                .map(UserBadge::getBadgeId)
                .collect(Collectors.toSet());

        List<Badge> newlyEarnedBadges = grantableBadges.stream()
                .filter(badge -> !earnedBadgeIds.contains(badge.getBadgeId()))
                .toList();

        List<UserBadge> savedUserBadges = userBadgePersistencePort.saveAll(
                newlyEarnedBadges.stream()
                        .map(badge -> UserBadge.earn(userId, badge.getBadgeId()))
                        .toList()
        );

        Map<Long, UserBadge> savedByBadgeId = savedUserBadges.stream()
                .collect(Collectors.toMap(
                        UserBadge::getBadgeId,
                        Function.identity()
                ));

        List<MyBadgeResult> results = newlyEarnedBadges.stream()
                .map(badge -> toMyBadgeResult(
                        savedByBadgeId.get(badge.getBadgeId()),
                        badge
                ))
                .toList();

        return new BadgeSyncResult(
                userId,
                totalPoint,
                results.size(),
                results
        );
    }


}
