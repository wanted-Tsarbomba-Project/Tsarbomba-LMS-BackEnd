package com.wanted.codebombalms.problems.recommendation.presentation.api.response;

import com.wanted.codebombalms.problems.recommendation.application.usecase.SaveProblemRecommendedCoursesUseCase.SaveProblemRecommendedCoursesResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record SaveProblemRecommendedCoursesResponse(
        @Schema(description = "문제 ID", example = "5164")
        Long problemId,

        @Schema(description = "저장된 추천 코스 개수", example = "2")
        Integer connectedCourseCount,

        @Schema(description = "저장된 추천 코스 ID 목록", example = "[10, 11]")
        List<Long> courseIds
) {
    public static SaveProblemRecommendedCoursesResponse from(SaveProblemRecommendedCoursesResult result) {
        return new SaveProblemRecommendedCoursesResponse(
                result.problemId(),
                result.connectedCourseCount(),
                result.courseIds()
        );
    }
}
