package com.wanted.codebombalms.recommendation.infrastructure.persistence;

import com.wanted.codebombalms.recommendation.application.port.ProblemRecommendationQueryPort;
import com.wanted.codebombalms.recommendation.domain.model.ProblemSetRecommendation;
import com.wanted.codebombalms.recommendation.infrastructure.metrics.RecommendationMetrics;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 추천 조회 port를 DB 조회로 구현하는 persistence adapter입니다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemRecommendationQueryAdapter implements ProblemRecommendationQueryPort {

    private final SpringDataProblemRecommendationRepository problemRecommendationRepository;
    private final RecommendationMetrics recommendationMetrics;

    /** repository projection을 추천 도메인 모델 목록으로 변환합니다. */
    @Override
    public List<ProblemSetRecommendation> findActiveProblemSetRecommendations(Long userId, int limit) {
        long startedAt = System.nanoTime();
        List<ProblemSetRecommendation> result = problemRecommendationRepository.findActiveProblemSetRecommendations(userId, limit)
                .stream()
                .map(row -> new ProblemSetRecommendation(
                        row.getRecommendationId(),
                        row.getProblemSetId(),
                        row.getRankNo(),
                        row.getTitle(),
                        row.getDescription(),
                        row.getDifficulty(),
                        row.getAccuracyRate(),
                        row.getCategoryId(),
                        row.getCategoryName()
                ))
                .toList();
        long elapsedNanos = System.nanoTime() - startedAt;

        recommendationMetrics.recordProblemSetListQuery(elapsedNanos);
        log.info("event=recommendation_problem_set_list_query_completed resultCount={} durationMs={}",
                result.size(), elapsedNanos / 1_000_000);

        return result;
    }
}
