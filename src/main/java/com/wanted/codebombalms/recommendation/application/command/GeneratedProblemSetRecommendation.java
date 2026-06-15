package com.wanted.codebombalms.recommendation.application.command;

import com.wanted.codebombalms.recommendation.domain.model.RecommendationAlgorithm;
import java.math.BigDecimal;

/** Python 추천 서버가 계산한 사용자별 문제 세트 추천 한 건을 표현합니다. */
public record GeneratedProblemSetRecommendation(
        Long problemSetId,
        BigDecimal support,
        BigDecimal confidence,
        BigDecimal lift,
        Integer rankNo,
        RecommendationAlgorithm algorithm
) {
}
