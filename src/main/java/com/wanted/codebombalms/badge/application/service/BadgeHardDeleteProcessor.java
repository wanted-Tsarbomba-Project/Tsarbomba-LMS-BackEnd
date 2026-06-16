package com.wanted.codebombalms.badge.application.service;

import com.wanted.codebombalms.badge.application.port.BadgeImageStoragePort;
import com.wanted.codebombalms.badge.application.port.BadgePersistencePort;
import com.wanted.codebombalms.badge.application.port.UserBadgePersistencePort;
import com.wanted.codebombalms.badge.domain.model.Badge;
import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BadgeHardDeleteProcessor {

    private final BadgePersistencePort badgePersistencePort;
    private final UserBadgePersistencePort userBadgePersistencePort;
    private final BadgeImageStoragePort badgeImageStoragePort;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean hardDelete(Badge badge) {
        try {
            badgeImageStoragePort.delete(badge.getObjectName());

            userBadgePersistencePort.deleteAllByBadgeId(badge.getBadgeId());
            badgePersistencePort.deleteById(badge.getBadgeId());

            return true;
        } catch (ExternalServiceException e) {
            log.error(
                    "배지 GCS 이미지 삭제 실패로 하드 삭제를 건너뜁니다. badgeId={}, objectName={}",
                    badge.getBadgeId(),
                    badge.getObjectName(),
                    e
            );

            return false;
        }
    }
}
