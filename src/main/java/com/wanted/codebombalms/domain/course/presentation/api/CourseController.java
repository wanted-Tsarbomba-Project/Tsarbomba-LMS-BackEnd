package com.wanted.codebombalms.domain.course.presentation.api;

import com.wanted.codebombalms.domain.course.application.command.CreateCourseCommand;
import com.wanted.codebombalms.domain.course.application.command.PublishCourseCommand;
import com.wanted.codebombalms.domain.course.application.command.UpdateCourseCommand;
import com.wanted.codebombalms.domain.course.application.usecase.CourseCommandUseCase;
import com.wanted.codebombalms.domain.course.application.usecase.CourseQueryUseCase;
import com.wanted.codebombalms.domain.course.presentation.api.request.CourseCreateRequest;
import com.wanted.codebombalms.domain.course.presentation.api.request.CourseUpdateRequest;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
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

    /**
     * 강좌 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> findAllCourses() {

        log.info("[CourseController] 강좌 목록 조회 요청");

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        ApiResponseCode.SUCCESS,
                        "강좌 목록 조회 성공",
                        courseQueryUseCase.findAllCourses()
                ));
    }

    /**
     * 강좌 상세 조회
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<?>> findCourseById(@PathVariable Long courseId) {

        log.info("[CourseController] 강좌 상세 조회 요청 - courseId: {}", courseId);

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        ApiResponseCode.SUCCESS,
                        "강좌 상세 조회 성공",
                        courseQueryUseCase.findCourseById(courseId)
                ));
    }

    /**
     * 강좌 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createCourse(
            @Valid @RequestBody CourseCreateRequest request
    ) {

        log.info("[CourseController] 강좌 등록 요청 - title: {}", request.title());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        ApiResponseCode.COURSE_CREATED,
                        "강좌 등록 성공",
                        courseCommandUseCase.createCourse(new CreateCourseCommand(
                                request.instructorId(),
                                request.title(),
                                request.description(),
                                request.thumbnailUrl()
                        ))
                ));
    }

    /**
     * 강좌 수정
     */
    @PutMapping("/{courseId}")
    public ResponseEntity<ApiResponse<?>> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseUpdateRequest request
    ) {

        log.info("[CourseController] 강좌 수정 요청 - courseId: {}", courseId);

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        ApiResponseCode.SUCCESS,
                        "강좌 수정 성공",
                        courseCommandUseCase.updateCourse(new UpdateCourseCommand(
                                courseId,
                                request.title(),
                                request.description(),
                                request.thumbnailUrl(),
                                request.status()
                        ))
                ));
    }

    /**
     * 강좌 삭제
     */
    @PatchMapping("/{courseId}/publish")
    public ResponseEntity<ApiResponse<?>> publishCourse(@PathVariable Long courseId) {

        log.info("[CourseController] 강좌 개설 요청 - courseId: {}", courseId);

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        ApiResponseCode.COURSE_PUBLISHED,
                        "강좌 개설 성공",
                        courseCommandUseCase.publishCourse(new PublishCourseCommand(courseId))
                ));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {

        log.info("[CourseController] 강좌 삭제 요청 - courseId: {}", courseId);

        courseCommandUseCase.deleteCourse(courseId);

        return ResponseEntity.noContent().build();
    }
}
