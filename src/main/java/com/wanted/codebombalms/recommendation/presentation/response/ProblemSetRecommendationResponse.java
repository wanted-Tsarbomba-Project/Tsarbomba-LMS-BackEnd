package com.wanted.codebombalms.recommendation.presentation.response;

import com.wanted.codebombalms.recommendation.domain.model.ProblemSetRecommendation;
import io.swagger.v3.oas.annotations.media.Schema;
/** 추천 문제 세트 한 건의 API 응답을 표현합니다. */
public record ProblemSetRecommendationResponse(
        @Schema(description = "추천 ID", example = "101")
        Long recommendationId,

        @Schema(description = "추천 문제 세트 ID", example = "3001")
        Long problemSetId,

        @Schema(description = "추천 순위", example = "1")
        Integer rankNo,

        @Schema(description = "문제 세트 제목", example = "pandas 기초 분석 문제 세트")
        String title,

        @Schema(description = "문제 세트 설명", example = "CSV 데이터를 불러와 기본 정보를 확인하는 문제 세트입니다.")
        String description,

        @Schema(description = "문제 세트 난이도", example = "EASY")
        String difficulty,

        @Schema(description = "문제 세트 정답률", example = "75.5")
        Double accuracyRate,

        @Schema(description = "문제 카테고리 ID", example = "10")
        Long categoryId,

        @Schema(description = "문제 카테고리명", example = "데이터 분석")
        String categoryName
) {

    /** 추천 도메인 모델을 API 응답 DTO로 변환합니다. */
    public static ProblemSetRecommendationResponse from(ProblemSetRecommendation recommendation) {
        return new ProblemSetRecommendationResponse(
                recommendation.recommendationId(),
                recommendation.problemSetId(),
                recommendation.rankNo(),
                recommendation.title(),
                recommendation.description(),
                recommendation.difficulty(),
                recommendation.accuracyRate(),
                recommendation.categoryId(),
                recommendation.categoryName()
        );
    }
}
