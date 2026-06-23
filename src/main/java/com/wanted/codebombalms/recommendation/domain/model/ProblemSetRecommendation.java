package com.wanted.codebombalms.recommendation.domain.model;

/** 학생에게 노출할 문제 세트 추천 한 건을 표현합니다. */
public record ProblemSetRecommendation(
        Long recommendationId,
        Long problemSetId,
        Integer rankNo,
        String title,
        String description,
        String difficulty,
        Double accuracyRate,
        Long categoryId,
        String categoryName
) {
}
