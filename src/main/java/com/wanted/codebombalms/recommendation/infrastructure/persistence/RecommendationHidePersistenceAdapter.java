package com.wanted.codebombalms.recommendation.infrastructure.persistence;

import com.wanted.codebombalms.recommendation.application.port.RecommendationHidePort;
import com.wanted.codebombalms.recommendation.domain.model.RecommendationHide;
import com.wanted.codebombalms.recommendation.domain.model.RecommendationHideType;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 추천 숨김 port를 recommendation_hide 테이블 저장/조회로 구현합니다. */
@Component
@RequiredArgsConstructor
public class RecommendationHidePersistenceAdapter implements RecommendationHidePort {

    private final SpringDataRecommendationHideRepository recommendationHideRepository;

    /** 사용자와 숨김 유형 기준의 전체 숨김 설정을 조회합니다. */
    @Override
    public Optional<RecommendationHide> findHide(Long userId, RecommendationHideType hideType) {
        return recommendationHideRepository.findByUserIdAndHideTypeAndTargetIdIsNull(userId, hideType)
                .map(RecommendationHideJpaEntity::toDomain);
    }

    /** 기존 숨김 row가 있으면 갱신하고 없으면 새로 생성합니다. */
    @Override
    public RecommendationHide saveOrUpdate(
            Long userId,
            RecommendationHideType hideType,
            LocalDateTime hiddenUntil
    ) {
        RecommendationHideJpaEntity entity = recommendationHideRepository
                .findByUserIdAndHideTypeAndTargetIdIsNull(userId, hideType)
                .orElseGet(() -> new RecommendationHideJpaEntity(userId, hideType, hiddenUntil));

        entity.updateHiddenUntil(hiddenUntil);

        return recommendationHideRepository.save(entity).toDomain();
    }
}
