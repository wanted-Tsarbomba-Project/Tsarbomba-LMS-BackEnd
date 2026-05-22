package com.wanted.codebombalms.enrollment.controller;

import com.wanted.codebombalms.enrollment.application.command.CancelEnrollmentCommand;
import com.wanted.codebombalms.enrollment.application.command.EnrollCourseCommand;
import com.wanted.codebombalms.enrollment.application.port.CourseCatalogPort;
import com.wanted.codebombalms.enrollment.application.usecase.EnrollmentCommandUseCase;
import com.wanted.codebombalms.enrollment.application.usecase.EnrollmentQueryUseCase;
import com.wanted.codebombalms.enrollment.presentation.api.request.EnrollCourseRequest;
import com.wanted.codebombalms.enrollment.presentation.api.response.EnrollCourseResponse;
import com.wanted.codebombalms.enrollment.presentation.api.response.MyCourseResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EnrollmentController {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentController.class);

    private final EnrollmentCommandUseCase enrollmentCommandUseCase;
    private final EnrollmentQueryUseCase enrollmentQueryUseCase;
    private final CourseCatalogPort courseCatalogPort;

    @PostMapping("/courses/{courseId}/enrollments")
    public ResponseEntity<ApiResponse<?>> createEnrollment(
            @PathVariable Long courseId,
            @Valid @RequestBody EnrollCourseRequest request
    ) {
        log.info("[EnrollmentController] create enrollment - courseId: {}, userId: {}", courseId, request.userId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        EnrollmentResponseCode.CREATED,
                        EnrollmentResponseMessage.CREATED,
                        EnrollCourseResponse.from(enrollmentCommandUseCase.createEnrollment(
                                new EnrollCourseCommand(request.userId(), courseId)
                        ))
                ));
    }

    @GetMapping("/students/{userId}/enrollments")
    public ResponseEntity<ApiResponse<?>> findMyCourses(@PathVariable Long userId) {
        log.info("[EnrollmentController] find my courses - userId: {}", userId);

        return ResponseEntity.ok(ApiResponse.success(
                EnrollmentResponseCode.RETRIEVED,
                EnrollmentResponseMessage.RETRIEVED,
                enrollmentQueryUseCase.findMyCourses(userId)
                        .stream()
                        .map(enrollment -> MyCourseResponse.from(
                                enrollment,
                                courseCatalogPort.getPublicationStatus(enrollment.getCourseId())
                        ))
                        .toList()
        ));
    }

    @DeleteMapping("/students/{userId}/enrollments/{enrollmentId}")
    public ResponseEntity<Void> cancelEnrollment(
            @PathVariable Long userId,
            @PathVariable Long enrollmentId
    ) {
        log.info("[EnrollmentController] cancel enrollment - userId: {}, enrollmentId: {}", userId, enrollmentId);

        enrollmentCommandUseCase.cancelEnrollment(new CancelEnrollmentCommand(userId, enrollmentId));

        return ResponseEntity.noContent().build();
    }
}
