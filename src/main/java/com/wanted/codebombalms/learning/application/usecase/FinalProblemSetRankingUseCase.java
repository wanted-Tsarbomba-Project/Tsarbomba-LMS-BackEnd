package com.wanted.codebombalms.learning.application.usecase;

import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient.LearningRecommendationResult;
import java.util.Set;

/** 사용자의 강좌 학습 기록을 기반으로 FINAL 문제세트 순위를 조회합니다. */
public interface FinalProblemSetRankingUseCase {

    LearningRecommendationResult rankFinalProblemSets(
            Long userId,
            Long courseId,
            Long lectureId,
            Long problemCategoryId,
            Set<Long> excludedProblemSetIds,
            boolean operator
    );
}
