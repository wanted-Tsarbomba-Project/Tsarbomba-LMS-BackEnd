package com.wanted.codebombalms.badge.application.usecase;

import com.wanted.codebombalms.badge.application.command.CreateBadgeCommand;
import com.wanted.codebombalms.badge.application.command.UpdateBadgeCommand;
import com.wanted.codebombalms.badge.application.query.BadgeResult;

import java.util.List;

public interface AdminBadgeUseCase {

    List<BadgeResult> getBadges();

    BadgeResult getBadge(Long badgeId);

    BadgeResult createBadge(CreateBadgeCommand command);

    BadgeResult updateBadge(Long badgeId, UpdateBadgeCommand command);

    void deleteBadge(Long badgeId);
}
