package com.wanted.codebombalms.recommendation.presentation.response;

import com.wanted.codebombalms.recommendation.application.command.RecommendationHideResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** 오늘 하루 숨김 처리 API 응답을 표현합니다. */
public record RecommendationHideTodayResponse(
        @Schema(description = "숨김 처리 여부", example = "true")
        boolean hidden,

        @Schema(description = "숨김 만료 시각", example = "2026-06-14T23:59:59")
        LocalDateTime hiddenUntil
) {

    /** 숨김 처리 결과를 API 응답 DTO로 변환합니다. */
    public static RecommendationHideTodayResponse from(RecommendationHideResult result) {
        return new RecommendationHideTodayResponse(
                result.hidden(),
                result.hiddenUntil()
        );
    }
}
