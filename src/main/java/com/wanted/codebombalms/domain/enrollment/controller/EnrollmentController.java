package com.wanted.codebombalms.domain.enrollment.controller;

import com.wanted.codebombalms.domain.enrollment.application.service.EnrollmentService;
import com.wanted.codebombalms.domain.enrollment.presentation.api.request.EnrollmentCreateRequest;
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

    private final EnrollmentService enrollmentService;

    @PostMapping("/courses/{courseId}/enrollments")
    public ResponseEntity<ApiResponse<?>> createEnrollment(
            @PathVariable Long courseId,
            @Valid @RequestBody EnrollmentCreateRequest request
    ) {
        log.info("[EnrollmentController] create enrollment - courseId: {}, studentId: {}",
                courseId,
                request.getStudentId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        EnrollmentResponseCode.CREATED,
                        EnrollmentResponseMessage.CREATED,
                        enrollmentService.createEnrollment(courseId, request)
                ));
    }

    @GetMapping("/students/{studentId}/enrollments")
    public ResponseEntity<ApiResponse<?>> findMyCourses(@PathVariable Long studentId) {
        log.info("[EnrollmentController] find my courses - studentId: {}", studentId);

        return ResponseEntity.ok(ApiResponse.success(
                EnrollmentResponseCode.RETRIEVED,
                EnrollmentResponseMessage.RETRIEVED,
                enrollmentService.findMyCourses(studentId)
        ));
    }

    @DeleteMapping("/students/{studentId}/enrollments/{enrollmentId}")
    public ResponseEntity<Void> cancelEnrollment(
            @PathVariable Long studentId,
            @PathVariable Long enrollmentId
    ) {
        log.info("[EnrollmentController] cancel enrollment - studentId: {}, enrollmentId: {}",
                studentId,
                enrollmentId
        );

        enrollmentService.cancelEnrollment(studentId, enrollmentId);

        return ResponseEntity.noContent().build();
    }
}
