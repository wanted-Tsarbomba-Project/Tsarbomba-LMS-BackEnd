package com.wanted.codebombalms.badge.application.query;

import java.time.LocalDateTime;

public record MyBadgeResult(
        Long badgeId,
        String badgeName,
        String description,
        Integer requiredPoint,
        String imageUrl,
        String status,
        LocalDateTime earnedAt,
        Boolean isEquipped
) {
}
