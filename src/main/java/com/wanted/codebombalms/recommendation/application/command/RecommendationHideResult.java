package com.wanted.codebombalms.recommendation.application.command;

import java.time.LocalDateTime;

/** 추천 숨김 처리 유스케이스의 결과를 표현합니다. */
public record RecommendationHideResult(
        boolean hidden,
        LocalDateTime hiddenUntil
) {
}
