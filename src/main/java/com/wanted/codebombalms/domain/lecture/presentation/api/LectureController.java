package com.wanted.codebombalms.domain.lecture.presentation.api;

import com.wanted.codebombalms.domain.lecture.presentation.api.request.LectureCreateRequest;
import com.wanted.codebombalms.domain.lecture.presentation.api.request.LectureUpdateRequest;
import com.wanted.codebombalms.domain.lecture.application.service.LectureService;
import com.wanted.codebombalms.global.presentation.api.commonLegacy.ResponseDTO;
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

    /**
     * 특정 강좌의 강의 목록 조회
     */
    @GetMapping("/courses/{courseId}/lectures")
    public ResponseEntity<ResponseDTO> findLecturesByCourseId(@PathVariable Long courseId) {

        log.info("[LectureController] 강의 목록 조회 요청 - courseId: {}", courseId);

        return ResponseEntity.ok()
                .body(new ResponseDTO(
                        HttpStatus.OK,
                        "강의 목록 조회 성공",
                        lectureService.findLecturesByCourseId(courseId)
                ));
    }

    /**
     * 강의 상세 조회
     */
    @GetMapping("/lectures/{lectureId}")
    public ResponseEntity<ResponseDTO> findLectureById(@PathVariable Long lectureId) {

        log.info("[LectureController] 강의 상세 조회 요청 - lectureId: {}", lectureId);

        return ResponseEntity.ok()
                .body(new ResponseDTO(
                        HttpStatus.OK,
                        "강의 상세 조회 성공",
                        lectureService.findLectureById(lectureId)
                ));
    }

    /**
     * 강의 등록
     */
    @PostMapping("/courses/{courseId}/lectures")
    public ResponseEntity<ResponseDTO> createLecture(
            @PathVariable Long courseId,
            @Valid @RequestBody LectureCreateRequest request
    ) {

        log.info("[LectureController] 강의 등록 요청 - courseId: {}, title: {}", courseId, request.getTitle());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO(
                        HttpStatus.CREATED,
                        "강의 등록 성공",
                        lectureService.createLecture(courseId, request)
                ));
    }

    /**
     * 강의 수정
     */
    @PutMapping("/lectures/{lectureId}")
    public ResponseEntity<ResponseDTO> updateLecture(
            @PathVariable Long lectureId,
            @Valid @RequestBody LectureUpdateRequest request
    ) {

        log.info("[LectureController] 강의 수정 요청 - lectureId: {}", lectureId);

        return ResponseEntity.ok()
                .body(new ResponseDTO(
                        HttpStatus.OK,
                        "강의 수정 성공",
                        lectureService.updateLecture(lectureId, request)
                ));
    }

    /**
     * 강의 삭제
     */
    @DeleteMapping("/lectures/{lectureId}")
    public ResponseEntity<Void> deleteLecture(@PathVariable Long lectureId) {

        log.info("[LectureController] 강의 삭제 요청 - lectureId: {}", lectureId);

        lectureService.deleteLecture(lectureId);

        return ResponseEntity.noContent().build();
    }
}