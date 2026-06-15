package com.wanted.codebombalms.recommendation.presentation.response;

import com.wanted.codebombalms.recommendation.application.query.ProblemSetRecommendationResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/** 추천 목록 조회 API 응답을 표현합니다. */
public record ProblemSetRecommendationListResponse(
        @Schema(description = "현재 추천 목록 숨김 여부", example = "false")
        boolean hidden,

        @Schema(description = "숨김 만료 시각", example = "2026-06-14T23:59:59", nullable = true)
        LocalDateTime hiddenUntil,

        @Schema(description = "추천 문제 세트 목록")
        List<ProblemSetRecommendationResponse> problemSets
) {

    /** 추천 목록 조회 결과를 API 응답 DTO로 변환합니다. */
    public static ProblemSetRecommendationListResponse from(ProblemSetRecommendationResult result) {
        return new ProblemSetRecommendationListResponse(
                result.hidden(),
                result.hiddenUntil(),
                result.problemSets().stream()
                        .map(ProblemSetRecommendationResponse::from)
                        .toList()
        );
    }
}
