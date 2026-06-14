package com.wanted.codebombalms.recommendation.infrastructure.persistence;

import com.wanted.codebombalms.recommendation.application.port.ProblemRecommendationQueryPort;
import com.wanted.codebombalms.recommendation.domain.model.ProblemSetRecommendation;
import com.wanted.codebombalms.recommendation.domain.model.RecommendationAlgorithm;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 추천 조회 port를 DB 조회로 구현하는 persistence adapter입니다. */
@Component
@RequiredArgsConstructor
public class ProblemRecommendationQueryAdapter implements ProblemRecommendationQueryPort {

    private final SpringDataProblemRecommendationRepository problemRecommendationRepository;

    /** repository projection을 추천 도메인 모델 목록으로 변환합니다. */
    @Override
    public List<ProblemSetRecommendation> findActiveProblemSetRecommendations(Long userId, int limit) {
        return problemRecommendationRepository.findActiveProblemSetRecommendations(userId, limit)
                .stream()
                .map(row -> new ProblemSetRecommendation(
                        row.getRecommendationId(),
                        row.getProblemSetId(),
                        row.getCategoryId(),
                        row.getCreatorId(),
                        row.getSupport(),
                        row.getConfidence(),
                        row.getLift(),
                        row.getRankNo(),
                        RecommendationAlgorithm.valueOf(row.getAlgorithm()),
                        row.getCreatedAt()
                ))
                .toList();
    }
}
