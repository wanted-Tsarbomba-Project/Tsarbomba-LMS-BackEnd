package com.wanted.codebombalms.recommendation.infrastructure.persistence;

import com.wanted.codebombalms.recommendation.application.command.GeneratedUserProblemSetRecommendations;
import com.wanted.codebombalms.recommendation.application.port.ProblemRecommendationCommandPort;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 추천 생성 결과 저장 port를 JPA repository로 구현하는 persistence adapter입니다. */
@Component
@RequiredArgsConstructor
public class ProblemRecommendationCommandAdapter implements ProblemRecommendationCommandPort {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final SpringDataProblemRecommendationRepository problemRecommendationRepository;

    /** 기존 ACTIVE 추천을 INACTIVE 처리한 뒤 Python 서버가 반환한 3개 추천을 ACTIVE로 저장합니다. */
    @Override
    public void replaceActiveRecommendations(GeneratedUserProblemSetRecommendations recommendations) {
        LocalDateTime now = LocalDateTime.now(SEOUL_ZONE);

        problemRecommendationRepository.deactivateActiveByUserId(recommendations.userId());
        problemRecommendationRepository.saveAll(recommendations.problemSets()
                .stream()
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
    }
}
