package com.wanted.codebombalms.problems.recommendation.presentation.api.response;

import com.wanted.codebombalms.problems.recommendation.application.usecase.GetRecommendedCourseEditViewUseCase.RecommendedCourseEditView;
import com.wanted.codebombalms.problems.recommendation.application.usecase.GetRecommendedCourseEditViewUseCase.SelectableCourseView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record RecommendedCourseEditViewResponse(
        @Schema(description = "문제 ID", example = "5164")
        Long problemId,

        @Schema(description = "현재 문제에 연결된 추천 코스 ID 목록", example = "[10, 11]")
        List<Long> selectedCourseIds,

        @Schema(description = "선택 가능한 코스 목록")
        List<SelectableCourseResponse> courses
) {
    public static RecommendedCourseEditViewResponse from(RecommendedCourseEditView view) {
        return new RecommendedCourseEditViewResponse(
                view.problemId(),
                view.selectedCourseIds(),
                view.courses().stream()
                        .map(SelectableCourseResponse::from)
                        .toList()
        );
    }

    public record SelectableCourseResponse(
            @Schema(description = "코스 ID", example = "10")
            Long courseId,

            @Schema(description = "코스 제목", example = "Pandas 데이터 분석 입문")
            String title,

            @Schema(description = "코스 설명", example = "Pandas를 활용한 데이터 분석 기초 코스입니다.")
            String description,

            @Schema(description = "코스 썸네일 URL", example = "/images/courses/pandas.png")
            String thumbnailUrl,

            @Schema(description = "현재 문제에 연결되어 있는지 여부", example = "true")
            Boolean selected,

            @Schema(description = "추천 노출 순서. 연결되지 않은 코스는 null", example = "1")
            Integer displayOrder
    ) {
        private static SelectableCourseResponse from(SelectableCourseView view) {
            return new SelectableCourseResponse(
                    view.courseId(),
                    view.title(),
                    view.description(),
                    view.thumbnailUrl(),
                    view.selected(),
                    view.displayOrder()
            );
        }
    }
}
