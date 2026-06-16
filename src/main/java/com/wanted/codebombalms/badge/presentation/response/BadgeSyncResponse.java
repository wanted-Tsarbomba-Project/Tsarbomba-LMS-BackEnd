package com.wanted.codebombalms.badge.presentation.response;

import com.wanted.codebombalms.badge.application.query.BadgeSyncResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "내 배지 동기화 응답")
public record BadgeSyncResponse(
        @Schema(description = "사용자의 현재 누적 포인트", example = "120")
        Integer totalPoint,

        @Schema(description = "이번 동기화로 새로 지급된 배지 수", example = "2")
        Integer newlyEarnedBadgeCount,

        @Schema(description = "이번 동기화로 새로 지급된 배지 목록")
        List<MyBadgeResponse> newlyEarnedBadges
) {
    public static BadgeSyncResponse from(BadgeSyncResult result) {
        return new BadgeSyncResponse(
                result.totalPoint(),
                result.newlyEarnedBadgeCount(),
                result.newlyEarnedBadges().stream()
                        .map(MyBadgeResponse::from)
                        .toList()
        );
    }
}
