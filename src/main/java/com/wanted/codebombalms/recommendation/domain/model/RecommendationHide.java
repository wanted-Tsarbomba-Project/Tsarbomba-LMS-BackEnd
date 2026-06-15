package com.wanted.codebombalms.recommendation.domain.model;

import java.time.LocalDateTime;

/** 사용자별 추천 숨김 설정과 만료 시간을 표현합니다. */
public record RecommendationHide(
        Long hideId,
        Long userId,
        RecommendationHideType hideType,
        Long targetId,
        LocalDateTime hiddenUntil,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /** 전달된 시각 기준으로 숨김이 아직 유효한지 확인합니다. */
    public boolean isHiddenAt(LocalDateTime now) {
        return hiddenUntil != null && hiddenUntil.isAfter(now);
    }
}
