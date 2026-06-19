package com.wanted.codebombalms.problems.recommendation.presentation.api.response;

import com.wanted.codebombalms.problems.recommendation.application.usecase.GetProblemRecommendedCoursesUseCase.RecommendedCourseView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record RecommendedCourseResponse(
        @Schema(description = "문제에 추천된 코스 목록")
        List<CourseResponse> courses
) {
    public static RecommendedCourseResponse from(List<RecommendedCourseView> courses) {
        return new RecommendedCourseResponse(
                courses.stream()
                        .map(CourseResponse::from)
                        .toList()
        );
    }

    public record CourseResponse(
            @Schema(description = "코스 ID", example = "10")
            Long courseId,

            @Schema(description = "코스 제목", example = "Pandas 데이터 분석 입문")
            String title,

            @Schema(description = "코스 설명", example = "Pandas를 활용한 데이터 분석 기초 코스입니다.")
            String description,

            @Schema(description = "코스 썸네일 URL", example = "/images/courses/pandas.png")
            String thumbnailUrl,

            @Schema(description = "추천 노출 순서", example = "1")
            Integer displayOrder
    ) {
        private static CourseResponse from(RecommendedCourseView view) {
            return new CourseResponse(
                    view.courseId(),
                    view.title(),
                    view.description(),
                    view.thumbnailUrl(),
                    view.displayOrder()
            );
        }
    }
}
