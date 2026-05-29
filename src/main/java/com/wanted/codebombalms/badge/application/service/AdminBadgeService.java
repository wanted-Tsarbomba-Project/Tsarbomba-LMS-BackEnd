package com.wanted.codebombalms.badge.application.service;

import com.wanted.codebombalms.badge.application.command.CreateBadgeCommand;
import com.wanted.codebombalms.badge.application.command.UpdateBadgeCommand;
import com.wanted.codebombalms.badge.application.query.BadgeResult;
import com.wanted.codebombalms.badge.application.usecase.AdminBadgeUseCase;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminBadgeService implements AdminBadgeUseCase {

    @Override
    public List<BadgeResult> getBadges() {
        return List.of();
    }

    @Override
    public BadgeResult getBadge(Long badgeId) {
        return null;
    }

    @Override
    public BadgeResult createBadge(CreateBadgeCommand command) {
        return null;
    }

    @Override
    public BadgeResult updateBadge(Long badgeId, UpdateBadgeCommand command) {
        return null;
    }

    @Override
    public void deleteBadge(Long badgeId) {
    }
}
