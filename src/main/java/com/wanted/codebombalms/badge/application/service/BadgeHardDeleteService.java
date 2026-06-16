package com.wanted.codebombalms.badge.application.service;

import com.wanted.codebombalms.badge.application.port.BadgePersistencePort;
import com.wanted.codebombalms.badge.application.usecase.HardDeleteBadgesUseCase;
import com.wanted.codebombalms.badge.domain.model.Badge;
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
    private final BadgeHardDeleteProcessor badgeHardDeleteProcessor;

    @Override
    @Transactional(readOnly = true)
    public int hardDeleteBefore(LocalDateTime threshold) {
        List<Badge> deletionTargets =
                badgePersistencePort.findDeletedBefore(threshold);

        int deletedCount = 0;

        for (Badge badge : deletionTargets) {
            if (badgeHardDeleteProcessor.hardDelete(badge)) {
                deletedCount++;
            }
        }

        return deletedCount;
    }


}
