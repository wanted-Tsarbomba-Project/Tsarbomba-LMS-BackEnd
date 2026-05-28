package com.wanted.codebombalms.ranking.presentation.response;

import com.wanted.codebombalms.ranking.application.query.RankingItem;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 포인트 랭킹 응답")
public record MyRankingResponse(
        @Schema(description = "내 랭킹 순위. 동점자는 같은 순위를 가집니다.", example = "5")
        Integer rank,

        @Schema(description = "내 회원 ID", example = "3")
        Long userId,

        @Schema(description = "내 닉네임", example = "user01")
        String nickname,

        @Schema(description = "내 누적 포인트", example = "120")
        Integer point
) {
    public static MyRankingResponse from(RankingItem item) {
        return new MyRankingResponse(
                item.rank(),
                item.userId(),
                item.nickname(),
                item.point()
        );
    }
}