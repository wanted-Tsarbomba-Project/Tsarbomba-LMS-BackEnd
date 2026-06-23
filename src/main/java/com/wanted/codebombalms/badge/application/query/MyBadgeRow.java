package com.wanted.codebombalms.badge.application.query;

import com.wanted.codebombalms.badge.domain.model.BadgeStatus;

import java.time.LocalDateTime;

public record MyBadgeRow(
        Long badgeId,
        String badgeName,
        String description,
        Integer requiredPoint,
        String objectName,
        BadgeStatus status,
        LocalDateTime earnedAt,
        boolean equipped
) {
}
