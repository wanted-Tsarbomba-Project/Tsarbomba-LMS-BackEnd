package com.wanted.codebombalms.badge.application.usecase;

import com.wanted.codebombalms.badge.application.query.BadgeSyncResult;
import com.wanted.codebombalms.badge.application.query.MyBadgeResult;

import java.util.List;

public interface MyBadgeUseCase {

    List<MyBadgeResult> getMyBadges(Long userId);

    MyBadgeResult equipBadge(Long userId, Long badgeId);

    BadgeSyncResult syncBadges(Long userId);
}
