package com.wanted.codebombalms.problems.recommendation.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record SaveProblemRecommendedCoursesRequest(
        @Schema(description = "추천 코스 ID 목록. 배열 순서가 추천 노출 순서가 됩니다.", example = "[10, 11]")
        @NotNull(message = "추천 코스 목록은 필수입니다.")
        List<@NotNull(message = "추천 코스 ID는 필수입니다.")
        @Positive(message = "추천 코스 ID는 1 이상이어야 합니다.") Long> courseIds
) {
}
