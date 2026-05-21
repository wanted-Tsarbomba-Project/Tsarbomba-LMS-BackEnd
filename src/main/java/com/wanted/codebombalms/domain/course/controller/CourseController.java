package com.wanted.codebombalms.domain.course.controller;

import com.wanted.codebombalms.domain.course.application.command.CreateCourseCommand;
import com.wanted.codebombalms.domain.course.application.command.PublishCourseCommand;
import com.wanted.codebombalms.domain.course.application.command.UpdateCourseCommand;
import com.wanted.codebombalms.domain.course.application.usecase.CourseCommandUseCase;
import com.wanted.codebombalms.domain.course.application.usecase.CourseQueryUseCase;
import com.wanted.codebombalms.domain.course.presentation.api.request.CourseCreateRequest;
import com.wanted.codebombalms.domain.course.presentation.api.request.CourseUpdateRequest;
import com.wanted.codebombalms.domain.course.presentation.api.response.CourseDetailResponse;
import com.wanted.codebombalms.domain.course.presentation.api.response.CourseResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private static final Logger log = LoggerFactory.getLogger(CourseController.class);

    private final CourseCommandUseCase courseCommandUseCase;
    private final CourseQueryUseCase courseQueryUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> findAllCourses() {
        log.info("[CourseController] find courses");

        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.RETRIEVED,
                CourseResponseMessage.RETRIEVED,
                courseQueryUseCase.findAllCourses()
                        .stream()
                        .map(CourseResponse::from)
                        .toList()
        ));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<?>> findCourseById(@PathVariable Long courseId) {
        log.info("[CourseController] find course - courseId: {}", courseId);

        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.RETRIEVED,
                CourseResponseMessage.RETRIEVED,
                CourseDetailResponse.from(courseQueryUseCase.findCourseById(courseId))
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createCourse(@Valid @RequestBody CourseCreateRequest request) {
        log.info("[CourseController] create course - title: {}", request.title());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        CourseResponseCode.CREATED,
                        CourseResponseMessage.CREATED,
                        CourseDetailResponse.from(courseCommandUseCase.createCourse(new CreateCourseCommand(
                                request.instructorId(),
                                request.title(),
                                request.description(),
                                request.thumbnailUrl()
                        )))
                ));
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<ApiResponse<?>> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseUpdateRequest request
    ) {
        log.info("[CourseController] update course - courseId: {}", courseId);

        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.UPDATED,
                CourseResponseMessage.UPDATED,
                CourseDetailResponse.from(courseCommandUseCase.updateCourse(new UpdateCourseCommand(
                        courseId,
                        request.title(),
                        request.description(),
                        request.thumbnailUrl(),
                        request.status()
                )))
        ));
    }

    @PatchMapping("/{courseId}/publish")
    public ResponseEntity<ApiResponse<?>> publishCourse(@PathVariable Long courseId) {
        log.info("[CourseController] publish course - courseId: {}", courseId);

        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.PUBLISHED,
                CourseResponseMessage.PUBLISHED,
                CourseDetailResponse.from(courseCommandUseCase.publishCourse(new PublishCourseCommand(courseId)))
        ));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        log.info("[CourseController] delete course - courseId: {}", courseId);

        courseCommandUseCase.deleteCourse(courseId);

        return ResponseEntity.noContent().build();
    }
}
