package com.wanted.codebombalms.domain.enrollment.controller;

import com.wanted.codebombalms.domain.enrollment.dto.request.EnrollmentCreateRequest;
import com.wanted.codebombalms.domain.enrollment.service.EnrollmentService;
import com.wanted.codebombalms.global.common.ResponseDTO;
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

    /**
     * 수강 신청
     */
    @PostMapping("/courses/{courseId}/enrollments")
    public ResponseEntity<ResponseDTO> createEnrollment(
            @PathVariable Long courseId,
            @Valid @RequestBody EnrollmentCreateRequest request
    ) {
        log.info("[EnrollmentController] 수강 신청 요청 - courseId: {}, studentId: {}",
                courseId,
                request.getStudentId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO(
                        HttpStatus.CREATED,
                        "수강 신청 성공",
                        enrollmentService.createEnrollment(courseId, request)
                ));
    }

    /**
     * 내 수강 강좌 목록 조회
     */
    @GetMapping("/students/{studentId}/enrollments")
    public ResponseEntity<ResponseDTO> findMyCourses(@PathVariable Long studentId) {

        log.info("[EnrollmentController] 내 수강 강좌 목록 조회 요청 - studentId: {}", studentId);

        return ResponseEntity.ok()
                .body(new ResponseDTO(
                        HttpStatus.OK,
                        "내 수강 강좌 목록 조회 성공",
                        enrollmentService.findMyCourses(studentId)
                ));
    }

    /**
     * 수강 신청 취소
     */
    @DeleteMapping("/students/{studentId}/enrollments/{enrollmentId}")
    public ResponseEntity<Void> cancelEnrollment(
            @PathVariable Long studentId,
            @PathVariable Long enrollmentId
    ) {
        log.info("[EnrollmentController] 수강 신청 취소 요청 - studentId: {}, enrollmentId: {}",
                studentId,
                enrollmentId
        );

        enrollmentService.cancelEnrollment(studentId, enrollmentId);

        return ResponseEntity.noContent().build();
    }

}