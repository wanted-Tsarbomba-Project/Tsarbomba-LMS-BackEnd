package com.wanted.codebombalms.recommendation.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 학생에게 노출할 문제 세트 추천 한 건을 표현합니다. */
public record ProblemSetRecommendation(
        Long recommendationId,
        Long problemSetId,
        Long categoryId,
        Long creatorId,
        BigDecimal support,
        BigDecimal confidence,
        BigDecimal lift,
        Integer rankNo,
        RecommendationAlgorithm algorithm,
        LocalDateTime createdAt
) {
}
