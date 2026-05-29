package com.wanted.codebombalms.ranking.presentation.response;

import com.wanted.codebombalms.ranking.application.query.RankingItem;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포인트 랭킹 항목 응답")
public record RankingResponse(
        @Schema(description = "랭킹 순위. 동점자는 같은 순위를 가집니다.", example = "1")
        Integer rank,

        @Schema(description = "회원 ID", example = "3")
        Long userId,

        @Schema(description = "회원 이름", example = "김학생")
        String name,

        @Schema(description = "회원 닉네임", example = "user01")
        String nickname,

        @Schema(description = "뱃지 이미지 URL. 뱃지 기능 구현 전에는 null입니다.", example = "/images/badges/problem-beginner.png", nullable = true)
        String badgeImageUrl,

        @Schema(description = "최근 7일 동안 획득한 주간 포인트", example = "30")
        Integer weeklyPoint,

        @Schema(description = "누적 포인트", example = "120")
        Integer totalPoint
) {
    public static RankingResponse from(RankingItem item) {
        return new RankingResponse(
                item.rank(),
                item.userId(),
                item.name(),
                item.nickname(),
                item.badgeImageUrl(),
                item.weeklyPoint(),
                item.totalPoint()
        );
    }
}
