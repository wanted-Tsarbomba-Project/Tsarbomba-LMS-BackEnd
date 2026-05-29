package com.wanted.codebombalms.badge.presentation.response;

import com.wanted.codebombalms.badge.application.query.BadgeResult;

import java.time.LocalDateTime;

public record BadgeResponse(
        Long badgeId,
        String badgeName,
        String description,
        Integer requiredPoint,
        String imageUrl,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BadgeResponse from(BadgeResult result) {
        return new BadgeResponse(
                result.badgeId(),
                result.badgeName(),
                result.description(),
                result.requiredPoint(),
                result.imageUrl(),
                result.status(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
