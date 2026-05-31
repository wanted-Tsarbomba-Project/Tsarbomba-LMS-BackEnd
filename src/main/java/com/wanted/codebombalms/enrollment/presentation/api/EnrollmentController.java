package com.wanted.codebombalms.enrollment.presentation.api;

import com.wanted.codebombalms.enrollment.application.command.CancelEnrollmentCommand;
import com.wanted.codebombalms.enrollment.application.command.EnrollCourseCommand;
import com.wanted.codebombalms.enrollment.application.port.CourseCatalogPort;
import com.wanted.codebombalms.enrollment.application.usecase.EnrollmentCommandUseCase;
import com.wanted.codebombalms.enrollment.application.usecase.EnrollmentQueryUseCase;
import com.wanted.codebombalms.enrollment.presentation.api.response.EnrollCourseResponse;
import com.wanted.codebombalms.enrollment.presentation.api.response.MyCourseResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "수강신청", description = "수강신청 API")
public class EnrollmentController {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentController.class);

    private final EnrollmentCommandUseCase enrollmentCommandUseCase;
    private final EnrollmentQueryUseCase enrollmentQueryUseCase;
    private final CourseCatalogPort courseCatalogPort;

    @PostMapping("/courses/{courseId}/enrollments")
    @Operation(summary = "수강신청 생성")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> createEnrollment(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        EnrollmentResponseCode.CREATED,
                        EnrollmentResponseMessage.CREATED,
                        EnrollCourseResponse.from(enrollmentCommandUseCase.createEnrollment(
                                new EnrollCourseCommand(userId, courseId)
                        ))
                ));
    }

    @GetMapping("/users/{userId}/enrollments")
    @Operation(summary = "학생 수강 목록 조회")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> findMyCourses(
            @PathVariable Long userId,
            @AuthenticationPrincipal Long authenticatedUserId
    ) {
        log.info("[EnrollmentController] find my courses - userId: {}", authenticatedUserId);

        return ResponseEntity.ok(ApiResponse.success(
                EnrollmentResponseCode.RETRIEVED,
                EnrollmentResponseMessage.RETRIEVED,
                enrollmentQueryUseCase.findMyCourses(authenticatedUserId)
                        .stream()
                        .map(enrollment -> MyCourseResponse.from(
                                enrollment,
                                courseCatalogPort.getPublicationStatus(enrollment.getCourseId())
                        ))
                        .toList()
        ));
    }

    @GetMapping("/enrollments")
    @Operation(summary = "전체 수강신청 목록 조회")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<?>> findAllEnrollments() {
        log.info("[EnrollmentController] find all active enrollments");

        return ResponseEntity.ok(ApiResponse.success(
                EnrollmentResponseCode.RETRIEVED,
                EnrollmentResponseMessage.RETRIEVED,
                enrollmentQueryUseCase.findAllActiveEnrollments()
                        .stream()
                        .map(enrollment -> MyCourseResponse.from(
                                enrollment,
                                courseCatalogPort.getPublicationStatus(enrollment.getCourseId())
                        ))
                        .toList()
        ));
    }

    @DeleteMapping("/users/{userId}/enrollments/{enrollmentId}")
    @Operation(summary = "수강신청 취소")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelEnrollment(
            @PathVariable Long userId,
            @PathVariable Long enrollmentId,
            @AuthenticationPrincipal Long authenticatedUserId
    ) {
        log.info("[EnrollmentController] cancel enrollment - userId: {}, enrollmentId: {}", authenticatedUserId, enrollmentId);

        enrollmentCommandUseCase.cancelEnrollment(new CancelEnrollmentCommand(authenticatedUserId, enrollmentId));

        return ResponseEntity.noContent().build();
    }
}
