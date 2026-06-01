package com.wanted.codebombalms.badge.application.query;

import java.util.List;

public record BadgeSyncResult(
        Long userId,
        Integer totalPoint,
        Integer newlyEarnedBadgeCount,
        List<MyBadgeResult> newlyEarnedBadges
) {
}
