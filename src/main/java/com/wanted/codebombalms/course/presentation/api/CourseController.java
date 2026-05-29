package com.wanted.codebombalms.course.presentation.api;

import com.wanted.codebombalms.course.application.command.CreateCourseCommand;
import com.wanted.codebombalms.course.application.command.PublishCourseCommand;
import com.wanted.codebombalms.course.application.command.UpdateCourseCommand;
import com.wanted.codebombalms.course.application.usecase.CourseCommandUseCase;
import com.wanted.codebombalms.course.application.usecase.CourseQueryUseCase;
import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.presentation.api.request.CourseCreateRequest;
import com.wanted.codebombalms.course.presentation.api.request.CourseUpdateRequest;
import com.wanted.codebombalms.course.presentation.api.response.CourseDetailResponse;
import com.wanted.codebombalms.course.presentation.api.response.CourseResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "강좌", description = "강좌 API")
public class CourseController {

    private static final Logger log = LoggerFactory.getLogger(CourseController.class);

    private final CourseCommandUseCase courseCommandUseCase;
    private final CourseQueryUseCase courseQueryUseCase;

    @Operation(summary = "강좌 목록 조회")
    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<?>> findAllCourses(Authentication authentication) {
        log.info("[CourseController] find courses");

        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.RETRIEVED,
                CourseResponseMessage.RETRIEVED,
                findCourses(null, authentication)
                        .stream()
                        .map(CourseResponse::from)
                        .toList()
        ));
    }

    @Operation(summary = "카테고리별 강좌 목록 조회")
    @GetMapping("/course-categories/{courseCategoryId}/courses")
    public ResponseEntity<ApiResponse<?>> findCoursesByCategory(
            @PathVariable Long courseCategoryId,
            Authentication authentication
    ) {
        log.info("[CourseController] find courses - courseCategoryId: {}", courseCategoryId);

        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.RETRIEVED,
                CourseResponseMessage.RETRIEVED,
                findCourses(courseCategoryId, authentication)
                        .stream()
                        .map(CourseResponse::from)
                        .toList()
        ));
    }

    @Operation(summary = "강좌 단건 조회")
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<ApiResponse<?>> findCourseById(
            @PathVariable Long courseId,
            Authentication authentication
    ) {
        log.info("[CourseController] find course - courseId: {}", courseId);

        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.RETRIEVED,
                CourseResponseMessage.RETRIEVED,
                CourseDetailResponse.from(isOperator(authentication)
                        ? courseQueryUseCase.findCourseByIdForOperator(courseId)
                        : courseQueryUseCase.findCourseById(courseId))
        ));
    }

    @Operation(summary = "강좌 생성")
    @PostMapping("/courses")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<?>> createCourse(
            @AuthenticationPrincipal Long instructorId,
            @Valid @RequestBody CourseCreateRequest request
    ) {
        log.info("[CourseController] create course - title: {}", request.title());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        CourseResponseCode.CREATED,
                        CourseResponseMessage.CREATED,
                        CourseDetailResponse.from(courseCommandUseCase.createCourse(new CreateCourseCommand(
                                instructorId,
                                request.courseCategoryId(),
                                request.title(),
                                request.description(),
                                request.thumbnailUrl()
                        )))
                ));
    }

    @Operation(summary = "강좌 수정")
    @PutMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('OPERATOR')")
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
                        request.courseCategoryId(),
                        request.title(),
                        request.description(),
                        request.thumbnailUrl(),
                        request.status()
                )))
        ));
    }

    @Operation(summary = "강좌 공개")
    @PatchMapping("/courses/{courseId}/publish")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<?>> publishCourse(@PathVariable Long courseId) {
        log.info("[CourseController] publish course - courseId: {}", courseId);

        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.PUBLISHED,
                CourseResponseMessage.PUBLISHED,
                CourseDetailResponse.from(courseCommandUseCase.publishCourse(new PublishCourseCommand(courseId)))
        ));
    }

    @Operation(summary = "강좌 삭제")
    @DeleteMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        log.info("[CourseController] delete course - courseId: {}", courseId);

        courseCommandUseCase.deleteCourse(courseId);

        return ResponseEntity.noContent().build();
    }

    private List<Course> findCourses(
            Long courseCategoryId,
            Authentication authentication
    ) {
        return isOperator(authentication)
                ? courseQueryUseCase.findAllCoursesForOperator(courseCategoryId)
                : courseQueryUseCase.findAllCourses(courseCategoryId);
    }

    private boolean isOperator(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_OPERATOR".equals(authority.getAuthority()));
    }
}
