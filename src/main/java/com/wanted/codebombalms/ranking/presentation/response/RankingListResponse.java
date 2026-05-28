package com.wanted.codebombalms.ranking.presentation.response;

import com.wanted.codebombalms.ranking.application.query.RankingListResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "포인트 랭킹 목록 응답")
public record RankingListResponse(
        @Schema(description = "랭킹 목록")
        List<RankingResponse> rankings
) {
    public static RankingListResponse from(RankingListResult result) {
        return new RankingListResponse(
                result.rankings().stream()
                        .map(RankingResponse::from)
                        .toList()
        );
    }
}