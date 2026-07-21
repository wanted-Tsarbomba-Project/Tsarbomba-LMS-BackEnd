package com.wanted.codebombalms.learning.application.port;

import java.math.BigDecimal;
import java.util.List;

/** Python FastAPI의 강좌 완료 문제세트 추천을 호출하는 출력 포트입니다. */
public interface LearningRecommendationClient {

    int MAX_RECOMMENDATION_COUNT = 2;

    LearningRecommendationResult rankFinalProblemSets(LearningRecommendationRequest request);

    record LearningRecommendationRequest(
            Long courseId,
            Long lectureId,
            Long problemCategoryId,
            List<Long> excludedProblemSetIds,
            int recommendationCount,
            LearningProfile learningProfile,
            LearningContext learningContext
    ) {
    }

    record LearningProfile(
            int totalMainProblemCount,
            int correctProblemCount,
            double directSolveRate,
            int explanationViewCount,
            double explanationViewRate,
            double averageAttemptCount,
            double averageTestPassRate
    ) {

        public static LearningProfile empty() {
            return new LearningProfile(0, 0, 0.0, 0, 0.0, 0.0, 0.0);
        }
    }

    record LearningContext(
            String courseTitle,
            String courseDescription,
            List<LectureContext> lectures
    ) {
    }

    record LectureContext(
            String title,
            String description
    ) {
    }

    record LearningRecommendationResult(
            String algorithm,
            List<RankedProblemSet> recommendations
    ) {
    }

    record RankedProblemSet(
            Long problemSetId,
            BigDecimal score,
            ReasonCode reasonCode,
            String recommendationReason
    ) {
    }

    enum ReasonCode {
        COURSE_RELATED,
        REVIEW_WEAK_AREA,
        LEVEL_MATCHED,
        NEXT_DIFFICULTY
    }
}
