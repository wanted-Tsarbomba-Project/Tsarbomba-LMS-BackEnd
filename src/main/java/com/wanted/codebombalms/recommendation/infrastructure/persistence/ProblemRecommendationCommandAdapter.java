package com.wanted.codebombalms.recommendation.infrastructure.persistence;

import com.wanted.codebombalms.recommendation.application.command.GeneratedUserProblemSetRecommendations;
import com.wanted.codebombalms.recommendation.application.port.ProblemRecommendationCommandPort;
import com.wanted.codebombalms.recommendation.domain.model.RecommendationStatus;
import com.wanted.codebombalms.recommendation.infrastructure.metrics.RecommendationMetrics;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 추천 생성 결과 저장 port를 JPA repository로 구현하는 persistence adapter입니다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemRecommendationCommandAdapter implements ProblemRecommendationCommandPort {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final SpringDataProblemRecommendationRepository problemRecommendationRepository;
    private final RecommendationMetrics recommendationMetrics;

    /** 기존 ACTIVE 추천을 INACTIVE 처리한 뒤 Python 서버가 반환한 3개 추천을 ACTIVE로 저장합니다. */
    @Override
    public void replaceActiveRecommendations(GeneratedUserProblemSetRecommendations recommendations) {
        long startedAt = System.nanoTime();
        LocalDateTime now = LocalDateTime.now(SEOUL_ZONE);

        try {
            problemRecommendationRepository.lockByUserIdAndStatus(recommendations.userId(), RecommendationStatus.ACTIVE);
            problemRecommendationRepository.deactivateActiveByUserId(recommendations.userId(), now);
            problemRecommendationRepository.saveAll(recommendations.problemSets()
                    .stream()
                    .peek(problemSet -> recommendationMetrics.recordScores(
                            problemSet.confidence(),
                            problemSet.lift(),
                            problemSet.support()
                    ))
                    .map(problemSet -> ProblemRecommendationJpaEntity.active(
                            recommendations.userId(),
                            problemSet.problemSetId(),
                            problemSet.support(),
                            problemSet.confidence(),
                            problemSet.lift(),
                            problemSet.rankNo(),
                            problemSet.algorithm(),
                            now
                    ))
                    .toList());

            long elapsedNanos = System.nanoTime() - startedAt;
            recommendationMetrics.recordGenerationSave("success", elapsedNanos);
            log.info("event=recommendation_generation_saved userId={} itemCount={} durationMs={}",
                    recommendations.userId(), recommendations.problemSets().size(), elapsedNanos / 1_000_000);
        } catch (RuntimeException exception) {
            recommendationMetrics.recordGenerationSave("failed", System.nanoTime() - startedAt);
            throw exception;
        }
    }
}
