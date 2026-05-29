package com.wanted.codebombalms.badge.presentation.response;

import com.wanted.codebombalms.badge.application.query.MyBadgeResult;

import java.time.LocalDateTime;

public record MyBadgeResponse(
        Long badgeId,
        String badgeName,
        String description,
        Integer requiredPoint,
        String imageUrl,
        String status,
        LocalDateTime earnedAt,
        Boolean isEquipped
) {
    public static MyBadgeResponse from(MyBadgeResult result) {
        return new MyBadgeResponse(
                result.badgeId(),
                result.badgeName(),
                result.description(),
                result.requiredPoint(),
                result.imageUrl(),
                result.status(),
                result.earnedAt(),
                result.isEquipped()
        );
    }
}
