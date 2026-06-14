package com.wanted.codebombalms.recommendation.application.port;

import com.wanted.codebombalms.recommendation.domain.model.RecommendationHide;
import com.wanted.codebombalms.recommendation.domain.model.RecommendationHideType;
import java.time.LocalDateTime;
import java.util.Optional;

/** 추천 숨김 설정 저장과 조회를 추상화합니다. */
public interface RecommendationHidePort {

    /** 사용자와 숨김 유형에 해당하는 전체 숨김 설정을 조회합니다. */
    Optional<RecommendationHide> findHide(Long userId, RecommendationHideType hideType);

    /** 사용자와 숨김 유형에 해당하는 전체 숨김 설정을 저장하거나 갱신합니다. */
    RecommendationHide saveOrUpdate(Long userId, RecommendationHideType hideType, LocalDateTime hiddenUntil);
}
