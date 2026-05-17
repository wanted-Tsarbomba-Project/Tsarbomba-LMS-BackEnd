package com.wanted.codebombalms.domain.course.controller;

import com.wanted.codebombalms.domain.course.dto.request.CourseCreateRequest;
import com.wanted.codebombalms.domain.course.dto.request.CourseUpdateRequest;
import com.wanted.codebombalms.domain.course.service.CourseService;
import com.wanted.codebombalms.global.common.ResponseDTO;
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

    private final CourseService courseService;

    /**
     * 강좌 목록 조회
     */
    @GetMapping
    public ResponseEntity<ResponseDTO> findAllCourses() {

        log.info("[CourseController] 강좌 목록 조회 요청");

        return ResponseEntity.ok()
                .body(new ResponseDTO(
                        HttpStatus.OK,
                        "강좌 목록 조회 성공",
                        courseService.findAllCourses()
                ));
    }

    /**
     * 강좌 상세 조회
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<ResponseDTO> findCourseById(@PathVariable Long courseId) {

        log.info("[CourseController] 강좌 상세 조회 요청 - courseId: {}", courseId);

        return ResponseEntity.ok()
                .body(new ResponseDTO(
                        HttpStatus.OK,
                        "강좌 상세 조회 성공",
                        courseService.findCourseById(courseId)
                ));
    }

    /**
     * 강좌 등록
     */
    @PostMapping
    public ResponseEntity<ResponseDTO> createCourse(
            @Valid @RequestBody CourseCreateRequest request
    ) {

        log.info("[CourseController] 강좌 등록 요청 - title: {}", request.getTitle());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO(
                        HttpStatus.CREATED,
                        "강좌 등록 성공",
                        courseService.createCourse(request)
                ));
    }

    /**
     * 강좌 수정
     */
    @PutMapping("/{courseId}")
    public ResponseEntity<ResponseDTO> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseUpdateRequest request
    ) {

        log.info("[CourseController] 강좌 수정 요청 - courseId: {}", courseId);

        return ResponseEntity.ok()
                .body(new ResponseDTO(
                        HttpStatus.OK,
                        "강좌 수정 성공",
                        courseService.updateCourse(courseId, request)
                ));
    }

    /**
     * 강좌 삭제
     */
    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {

        log.info("[CourseController] 강좌 삭제 요청 - courseId: {}", courseId);

        courseService.deleteCourse(courseId);

        return ResponseEntity.noContent().build();
    }
}