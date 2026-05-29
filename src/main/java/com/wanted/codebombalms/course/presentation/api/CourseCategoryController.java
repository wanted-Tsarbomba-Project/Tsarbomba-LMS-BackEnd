package com.wanted.codebombalms.course.presentation.api;

import com.wanted.codebombalms.course.application.usecase.CourseCategoryQueryUseCase;
import com.wanted.codebombalms.course.presentation.api.response.CourseCategoryResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/course-categories")
@RequiredArgsConstructor
public class CourseCategoryController {

    private final CourseCategoryQueryUseCase courseCategoryQueryUseCase;

    @GetMapping
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
