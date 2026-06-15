package com.wanted.codebombalms.recommendation.application.service;

import com.wanted.codebombalms.recommendation.application.command.GeneratedUserProblemSetRecommendations;
import com.wanted.codebombalms.recommendation.application.port.ProblemRecommendationCommandPort;
import com.wanted.codebombalms.recommendation.application.port.ProblemRecommendationGenerationClient;
import com.wanted.codebombalms.recommendation.application.usecase.GenerateProblemSetRecommendationsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Python 추천 서버 호출 결과를 검증하고 problem_recommendation에 반영합니다. */
@Service
@RequiredArgsConstructor
public class ProblemRecommendationGenerationService implements GenerateProblemSetRecommendationsUseCase {

    private static final int EXPECTED_RECOMMENDATION_COUNT = 3;

    private final ProblemRecommendationGenerationClient generationClient;
    private final ProblemRecommendationCommandPort commandPort;

    /** 사용자별 추천 3개 반환을 전제로 기존 추천 교체 저장을 수행합니다. */
    @Override
    @Transactional
    public int generate() {
        var generatedRecommendations = generationClient.generateProblemSetRecommendations();

        generatedRecommendations.forEach(this::validateRecommendationCount);
        generatedRecommendations.forEach(commandPort::replaceActiveRecommendations);

        return generatedRecommendations.size();
    }

    /** 추천 대상 사용자는 Python 서버에서 항상 3개의 문제 세트를 반환해야 합니다. */
    private void validateRecommendationCount(GeneratedUserProblemSetRecommendations recommendations) {
        int actualCount = recommendations.problemSets() == null ? 0 : recommendations.problemSets().size();

        if (actualCount != EXPECTED_RECOMMENDATION_COUNT) {
            throw new IllegalStateException(
                    "Python 추천 서버는 추천 대상 사용자별 문제 세트 3개를 반환해야 합니다. userId="
                            + recommendations.userId()
                            + ", actualCount="
                            + actualCount
            );
        }
    }
}
