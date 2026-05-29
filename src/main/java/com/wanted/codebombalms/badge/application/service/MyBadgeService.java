package com.wanted.codebombalms.badge.application.service;

import com.wanted.codebombalms.badge.application.query.BadgeSyncResult;
import com.wanted.codebombalms.badge.application.query.MyBadgeResult;
import com.wanted.codebombalms.badge.application.usecase.MyBadgeUseCase;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MyBadgeService implements MyBadgeUseCase {

    @Override
    public List<MyBadgeResult> getMyBadges(Long userId) {
        return List.of();
    }

    @Override
    public MyBadgeResult equipBadge(Long userId, Long badgeId) {
        return null;
    }

    @Override
    public BadgeSyncResult syncBadges(Long userId) {
        return new BadgeSyncResult(
                userId,
                0,
                0,
                List.of()
        );
    }
}
