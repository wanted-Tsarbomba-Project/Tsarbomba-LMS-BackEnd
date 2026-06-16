package com.wanted.codebombalms.badge.application.service;

import com.wanted.codebombalms.badge.application.port.BadgeImageStoragePort;
import com.wanted.codebombalms.badge.application.port.BadgePersistencePort;
import com.wanted.codebombalms.badge.application.port.UserBadgePersistencePort;
import com.wanted.codebombalms.badge.application.usecase.HardDeleteBadgesUseCase;
import com.wanted.codebombalms.badge.domain.model.Badge;
import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BadgeHardDeleteService implements HardDeleteBadgesUseCase {

    private final BadgePersistencePort badgePersistencePort;
    private final UserBadgePersistencePort userBadgePersistencePort;
    private final BadgeImageStoragePort badgeImageStoragePort;

    @Override
    public int hardDeleteBefore(LocalDateTime threshold) {
        List<Badge> deletionTargets =
                badgePersistencePort.findDeletedBefore(threshold);

        int deletedCount = 0;

        for (Badge badge : deletionTargets) {
            if (hardDelete(badge)) {
                deletedCount++;
            }
        }

        return deletedCount;
    }

    private boolean hardDelete(Badge badge) {
        try {
            badgeImageStoragePort.delete(badge.getObjectName());
        } catch (ExternalServiceException e) {
            log.error(
                    "뱃지 GCS 이미지 삭제 실패로 하드 딜리트를 건너뜁니다. badgeId={}, objectName={}",
                    badge.getBadgeId(),
                    badge.getObjectName(),
                    e
            );

            return false;
        }

        userBadgePersistencePort.deleteAllByBadgeId(badge.getBadgeId());
        badgePersistencePort.deleteById(badge.getBadgeId());

        return true;
    }
}
