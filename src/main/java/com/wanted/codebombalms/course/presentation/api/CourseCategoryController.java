package com.wanted.codebombalms.course.presentation.api;

import com.wanted.codebombalms.course.application.usecase.CourseCategoryQueryUseCase;
import com.wanted.codebombalms.course.presentation.api.response.CourseCategoryResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/course-categories")
@RequiredArgsConstructor
@Tag(name = "강좌 카테고리", description = "강좌 카테고리 API")
public class CourseCategoryController {

    private final CourseCategoryQueryUseCase courseCategoryQueryUseCase;

    @GetMapping
    @Operation(summary = "강좌 카테고리 목록 조회")
    public ResponseEntity<ApiResponse<?>> findCourseCategories() {
        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.RETRIEVED,
                CourseResponseMessage.RETRIEVED,
                courseCategoryQueryUseCase.findCourseCategories()
                        .stream()
                        .map(CourseCategoryResponse::from)
                        .toList()
        ));
    }
}
