package com.wanted.codebombalms.badge.presentation.response;

import com.wanted.codebombalms.badge.application.query.BadgeSyncResult;

import java.util.List;

public record BadgeSyncResponse(
        Long userId,
        Integer totalPoint,
        Integer newlyEarnedBadgeCount,
        List<MyBadgeResponse> newlyEarnedBadges
) {
    public static BadgeSyncResponse from(BadgeSyncResult result) {
        return new BadgeSyncResponse(
                result.userId(),
                result.totalPoint(),
                result.newlyEarnedBadgeCount(),
                result.newlyEarnedBadges().stream()
                        .map(MyBadgeResponse::from)
                        .toList()
        );
    }
}
