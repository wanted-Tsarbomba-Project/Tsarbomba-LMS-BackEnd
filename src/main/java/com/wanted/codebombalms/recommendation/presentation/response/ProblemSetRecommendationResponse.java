package com.wanted.codebombalms.recommendation.presentation.response;

import com.wanted.codebombalms.recommendation.domain.model.ProblemSetRecommendation;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 추천 문제 세트 한 건의 API 응답을 표현합니다. */
public record ProblemSetRecommendationResponse(
        @Schema(description = "추천 ID", example = "101")
        Long recommendationId,

        @Schema(description = "추천 문제 세트 ID", example = "3001")
        Long problemSetId,

        @Schema(description = "문제 카테고리 ID", example = "10")
        Long categoryId,

        @Schema(description = "문제 세트 생성자 ID", example = "7")
        Long creatorId,

        @Schema(description = "Apriori support", example = "0.034000")
        BigDecimal support,

        @Schema(description = "Apriori confidence", example = "0.720000")
        BigDecimal confidence,

        @Schema(description = "Apriori lift", example = "1.850000")
        BigDecimal lift,

        @Schema(description = "추천 순위", example = "1")
        Integer rankNo,

        @Schema(description = "추천 알고리즘", example = "APRIORI")
        String algorithm,

        @Schema(description = "추천 생성일", example = "2026-06-14T02:00:25")
        LocalDateTime createdAt
) {

    /** 추천 도메인 모델을 API 응답 DTO로 변환합니다. */
    public static ProblemSetRecommendationResponse from(ProblemSetRecommendation recommendation) {
        return new ProblemSetRecommendationResponse(
                recommendation.recommendationId(),
                recommendation.problemSetId(),
                recommendation.categoryId(),
                recommendation.creatorId(),
                recommendation.support(),
                recommendation.confidence(),
                recommendation.lift(),
                recommendation.rankNo(),
                recommendation.algorithm().name(),
                recommendation.createdAt()
        );
    }
}
