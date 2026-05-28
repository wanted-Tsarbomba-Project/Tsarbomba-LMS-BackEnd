package com.wanted.codebombalms.ranking.presentation.response;

import com.wanted.codebombalms.ranking.application.query.RankingItem;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포인트 랭킹 항목 응답")
public record RankingResponse(
        @Schema(description = "랭킹 순위. 동점자는 같은 순위를 가집니다.", example = "1")
        Integer rank,

        @Schema(description = "회원 ID", example = "3")
        Long userId,

        @Schema(description = "회원 닉네임", example = "user01")
        String nickname,

        @Schema(description = "랭킹 기준 포인트", example = "120")
        Integer point
) {
    public static RankingResponse from(RankingItem item) {
        return new RankingResponse(
                item.rank(),
                item.userId(),
                item.nickname(),
                item.point()
        );
    }
}