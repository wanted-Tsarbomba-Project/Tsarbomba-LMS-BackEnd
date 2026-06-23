package com.wanted.codebombalms.recommendation.infrastructure.persistence;

import com.wanted.codebombalms.recommendation.domain.model.RecommendationHideType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** recommendation_hide 저장과 조회를 담당하는 Spring Data JPA repository입니다. */
public interface SpringDataRecommendationHideRepository
        extends JpaRepository<RecommendationHideJpaEntity, Long> {

    /** target_id가 NULL인 사용자별 전체 숨김 설정을 조회합니다. */
    Optional<RecommendationHideJpaEntity> findByUserIdAndHideTypeAndTargetIdIsNull(
            Long userId,
            RecommendationHideType hideType
    );
}
