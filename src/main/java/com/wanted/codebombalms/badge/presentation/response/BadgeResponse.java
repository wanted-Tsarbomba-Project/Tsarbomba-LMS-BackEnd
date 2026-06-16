package com.wanted.codebombalms.badge.presentation.response;

import com.wanted.codebombalms.badge.application.query.BadgeResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "관리자 배지 응답")
public record BadgeResponse(
        @Schema(description = "배지 ID", example = "1")
        Long badgeId,

        @Schema(description = "배지 이름", example = "첫 정답 배지")
        String badgeName,

        @Schema(description = "배지 설명", example = "첫 문제를 맞힌 사용자에게 지급되는 배지입니다.")
        String description,

        @Schema(description = "배지 지급 기준 누적 포인트", example = "10")
        Integer requiredPoint,

        @Schema(description = "배지 이미지 접근 URL", example = "https://storage.googleapis.com/codebombalms/badge_image/example.png?X-Goog-Signature=example")
        String imageUrl,

        @Schema(description = "배지 상태", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
        String status,

        @Schema(description = "배지 생성 일시", example = "2026-06-16T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "배지 수정 일시", example = "2026-06-16T10:30:00")
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
