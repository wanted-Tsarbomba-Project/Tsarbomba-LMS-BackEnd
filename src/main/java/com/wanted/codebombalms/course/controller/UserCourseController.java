package com.wanted.codebombalms.course.controller;

import com.wanted.codebombalms.course.application.usecase.CourseQueryUseCase;
import com.wanted.codebombalms.course.presentation.api.response.CourseResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserCourseController {

    private static final Logger log = LoggerFactory.getLogger(UserCourseController.class);

    private final CourseQueryUseCase courseQueryUseCase;

    @GetMapping("/{userId}/courses")
    public ResponseEntity<ApiResponse<?>> findCoursesByInstructor(@PathVariable Long userId) {
        log.info("[InstructorCourseController] find instructor courses - userId: {}", userId);

        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.RETRIEVED,
                CourseResponseMessage.RETRIEVED,
                courseQueryUseCase.findCoursesByInstructor(userId)
                        .stream()
                        .map(CourseResponse::from)
                        .toList()
        ));
    }
}
