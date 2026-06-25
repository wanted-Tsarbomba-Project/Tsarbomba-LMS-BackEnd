package com.wanted.codebombalms.badge.application.service;

import com.wanted.codebombalms.badge.application.port.BadgeImageStoragePort;
import com.wanted.codebombalms.badge.application.port.BadgePersistencePort;
import com.wanted.codebombalms.badge.application.port.LoadMyBadgesPort;
import com.wanted.codebombalms.badge.application.port.LoadUserTotalPointPort;
import com.wanted.codebombalms.badge.application.port.UserBadgePersistencePort;
import com.wanted.codebombalms.badge.application.query.BadgeSyncResult;
import com.wanted.codebombalms.badge.application.query.MyBadgeRow;
import com.wanted.codebombalms.badge.application.query.MyBadgeResult;
import com.wanted.codebombalms.badge.application.usecase.MyBadgeUseCase;
import com.wanted.codebombalms.badge.application.usecase.SyncUserBadgesUseCase;
import com.wanted.codebombalms.badge.domain.model.Badge;
import com.wanted.codebombalms.badge.domain.model.UserBadge;
import com.wanted.codebombalms.badge.exception.BadgeErrorCode;
import com.wanted.codebombalms.badge.infrastructure.metrics.BadgeMetrics;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class MyBadgeService implements MyBadgeUseCase , SyncUserBadgesUseCase {

    private final UserBadgePersistencePort userBadgePersistencePort;
    private final BadgePersistencePort badgePersistencePort;
    private final BadgeImageStoragePort badgeImageStoragePort;
    private final LoadUserTotalPointPort loadUserTotalPointPort;
    private final LoadMyBadgesPort loadMyBadgesPort;
    private final BadgeMetrics badgeMetrics;

    @Override
    @Transactional(readOnly = true)
    public List<MyBadgeResult> getMyBadges(Long userId) {
        return loadMyBadgesPort.loadMyBadges(userId)
                .stream()
                .map(this::toMyBadgeResult)
                .toList();
    }

    private MyBadgeResult toMyBadgeResult(MyBadgeRow row) {
        return new MyBadgeResult(
                row.badgeId(),
                row.badgeName(),
                row.description(),
                row.requiredPoint(),
                generateImageUrl(row.objectName()),
                row.status().name(),
                row.earnedAt(),
                row.equipped()
        );
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
    @Transactional
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
    @Transactional
    public BadgeSyncResult syncBadges(Long userId) {
        int totalPoint = loadUserTotalPointPort.loadTotalPoint(userId);
        return sync(userId, totalPoint);
    }

    @Override
    @Transactional
    public BadgeSyncResult sync(Long userId, int totalPoint) {
        long syncStartNanos = System.nanoTime();
        long grantableLookupStartNanos = System.nanoTime();
        List<Badge> grantableBadges =
                badgePersistencePort.findGrantableBadges(totalPoint);
        long grantableLookupNanos = System.nanoTime() - grantableLookupStartNanos;
        badgeMetrics.recordGrantableLookup(grantableLookupNanos);

        long earnedLookupStartNanos = System.nanoTime();
        Set<Long> earnedBadgeIds = userBadgePersistencePort
                .findAllByUserId(userId)
                .stream()
                .map(UserBadge::getBadgeId)
                .collect(Collectors.toSet());
        long earnedLookupNanos = System.nanoTime() - earnedLookupStartNanos;
        badgeMetrics.recordEarnedLookup(earnedLookupNanos);

        List<Badge> newlyEarnedBadges = grantableBadges.stream()
                .filter(badge -> !earnedBadgeIds.contains(badge.getBadgeId()))
                .toList();

        long saveStartNanos = System.nanoTime();
        List<UserBadge> savedUserBadges = userBadgePersistencePort.insertIgnoreAll(
                newlyEarnedBadges.stream()
                        .map(badge -> UserBadge.earn(userId, badge.getBadgeId()))
                        .toList()
        );
        long saveNanos = System.nanoTime() - saveStartNanos;
        badgeMetrics.recordSave(saveNanos);

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

        BadgeSyncResult result = new BadgeSyncResult(
                userId,
                totalPoint,
                results.size(),
                results
        );

        long syncNanos = System.nanoTime() - syncStartNanos;
        badgeMetrics.recordSync(syncNanos);
        log.info(
                "event=badge_sync_completed userId={} totalPoint={} grantableBadgeCount={} earnedBadgeCount={} newlyEarnedBadgeCount={} grantableLookupMs={} earnedLookupMs={} saveMs={} durationMs={}",
                userId,
                totalPoint,
                grantableBadges.size(),
                earnedBadgeIds.size(),
                results.size(),
                elapsedMillis(grantableLookupNanos),
                elapsedMillis(earnedLookupNanos),
                elapsedMillis(saveNanos),
                elapsedMillis(syncNanos)
        );

        return result;
    }

    private long elapsedMillis(long elapsedNanos) {
        return elapsedNanos / 1_000_000;
    }


}
