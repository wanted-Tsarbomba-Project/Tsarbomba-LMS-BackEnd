package com.wanted.codebombalms.recommendation.application.query;

import com.wanted.codebombalms.recommendation.domain.model.ProblemSetRecommendation;
import java.time.LocalDateTime;
import java.util.List;

/** 추천 목록 조회 유스케이스의 결과를 표현합니다. */
public record ProblemSetRecommendationResult(
        boolean hidden,
        LocalDateTime hiddenUntil,
        List<ProblemSetRecommendation> problemSets
) {
}
