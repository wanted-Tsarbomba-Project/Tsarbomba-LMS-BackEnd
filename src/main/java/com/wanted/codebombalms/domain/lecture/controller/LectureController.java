package com.wanted.codebombalms.domain.lecture.controller;

import com.wanted.codebombalms.domain.lecture.application.service.LectureService;
import com.wanted.codebombalms.domain.lecture.presentation.api.request.LectureCreateRequest;
import com.wanted.codebombalms.domain.lecture.presentation.api.request.LectureUpdateRequest;
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
public class LectureController {

    private static final Logger log = LoggerFactory.getLogger(LectureController.class);

    private final LectureService lectureService;

    @GetMapping("/courses/{courseId}/lectures")
    public ResponseEntity<ApiResponse<?>> findLecturesByCourseId(@PathVariable Long courseId) {
        log.info("[LectureController] find lectures - courseId: {}", courseId);

        return ResponseEntity.ok(ApiResponse.success(
                LectureResponseCode.RETRIEVED,
                LectureResponseMessage.RETRIEVED,
                lectureService.findLecturesByCourseId(courseId)
        ));
    }

    @GetMapping("/lectures/{lectureId}")
    public ResponseEntity<ApiResponse<?>> findLectureById(@PathVariable Long lectureId) {
        log.info("[LectureController] find lecture - lectureId: {}", lectureId);

        return ResponseEntity.ok(ApiResponse.success(
                LectureResponseCode.RETRIEVED,
                LectureResponseMessage.RETRIEVED,
                lectureService.findLectureById(lectureId)
        ));
    }

    @PostMapping("/courses/{courseId}/lectures")
    public ResponseEntity<ApiResponse<?>> createLecture(
            @PathVariable Long courseId,
            @Valid @RequestBody LectureCreateRequest request
    ) {
        log.info("[LectureController] create lecture - courseId: {}, title: {}", courseId, request.getTitle());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        LectureResponseCode.CREATED,
                        LectureResponseMessage.CREATED,
                        lectureService.createLecture(courseId, request)
                ));
    }

    @PutMapping("/lectures/{lectureId}")
    public ResponseEntity<ApiResponse<?>> updateLecture(
            @PathVariable Long lectureId,
            @Valid @RequestBody LectureUpdateRequest request
    ) {
        log.info("[LectureController] update lecture - lectureId: {}", lectureId);

        return ResponseEntity.ok(ApiResponse.success(
                LectureResponseCode.UPDATED,
                LectureResponseMessage.UPDATED,
                lectureService.updateLecture(lectureId, request)
        ));
    }

    @DeleteMapping("/lectures/{lectureId}")
    public ResponseEntity<Void> deleteLecture(@PathVariable Long lectureId) {
        log.info("[LectureController] delete lecture - lectureId: {}", lectureId);

        lectureService.deleteLecture(lectureId);

        return ResponseEntity.noContent().build();
    }
}
