package com.wanted.codebombalms.lecture.presentation.api;

import com.wanted.codebombalms.lecture.application.command.CreateLectureCommand;
import com.wanted.codebombalms.lecture.application.command.UpdateLectureCommand;
import com.wanted.codebombalms.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.codebombalms.lecture.application.usecase.LectureQueryUseCase;
import com.wanted.codebombalms.lecture.presentation.api.request.LectureCreateRequest;
import com.wanted.codebombalms.lecture.presentation.api.request.LectureUpdateRequest;
import com.wanted.codebombalms.lecture.presentation.api.response.LectureDetailResponse;
import com.wanted.codebombalms.lecture.presentation.api.response.LectureResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "강의", description = "강의 API")
public class LectureController {

    private static final Logger log = LoggerFactory.getLogger(LectureController.class);

    private final LectureCommandUseCase lectureCommandUseCase;
    private final LectureQueryUseCase lectureQueryUseCase;

    @GetMapping("/courses/{courseId}/lectures")
    @Operation(summary = "강좌별 강의 목록 조회")
    public ResponseEntity<ApiResponse<?>> findLecturesByCourseId(@PathVariable Long courseId) {
        log.info("[LectureController] find lectures - courseId: {}", courseId);

        return ResponseEntity.ok(ApiResponse.success(
                LectureResponseCode.RETRIEVED,
                LectureResponseMessage.RETRIEVED,
                lectureQueryUseCase.findLecturesByCourseId(courseId)
                        .stream()
                        .map(LectureResponse::from)
                        .toList()
        ));
    }

    @GetMapping("/lectures/{lectureId}")
    @Operation(summary = "강의 단건 조회")
    public ResponseEntity<ApiResponse<?>> findLectureById(@PathVariable Long lectureId) {
        log.info("[LectureController] find lecture - lectureId: {}", lectureId);

        return ResponseEntity.ok(ApiResponse.success(
                LectureResponseCode.RETRIEVED,
                LectureResponseMessage.RETRIEVED,
                LectureDetailResponse.from(lectureQueryUseCase.findLectureById(lectureId))
        ));
    }

    @PostMapping("/courses/{courseId}/lectures")
    @Operation(summary = "강의 생성")
    public ResponseEntity<ApiResponse<?>> createLecture(
            @PathVariable Long courseId,
            @Valid @RequestBody LectureCreateRequest request
    ) {
        log.info("[LectureController] create lecture - courseId: {}, title: {}", courseId, request.title());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        LectureResponseCode.CREATED,
                        LectureResponseMessage.CREATED,
                        LectureDetailResponse.from(lectureCommandUseCase.createLecture(new CreateLectureCommand(
                                courseId,
                                request.title(),
                                request.description(),
                                request.videoUrl(),
                                request.thumbnailUrl(),
                                request.lectureOrder(),
                                request.status()
                        )))
                ));
    }

    @PutMapping("/lectures/{lectureId}")
    @Operation(summary = "강의 수정")
    public ResponseEntity<ApiResponse<?>> updateLecture(
            @PathVariable Long lectureId,
            @Valid @RequestBody LectureUpdateRequest request
    ) {
        log.info("[LectureController] update lecture - lectureId: {}", lectureId);

        return ResponseEntity.ok(ApiResponse.success(
                LectureResponseCode.UPDATED,
                LectureResponseMessage.UPDATED,
                LectureDetailResponse.from(lectureCommandUseCase.updateLecture(new UpdateLectureCommand(
                        lectureId,
                        request.title(),
                        request.description(),
                        request.videoUrl(),
                        request.thumbnailUrl(),
                        request.lectureOrder(),
                        request.status()
                )))
        ));
    }

    @DeleteMapping("/lectures/{lectureId}")
    @Operation(summary = "강의 삭제")
    public ResponseEntity<Void> deleteLecture(@PathVariable Long lectureId) {
        log.info("[LectureController] delete lecture - lectureId: {}", lectureId);

        lectureCommandUseCase.deleteLecture(lectureId);

        return ResponseEntity.noContent().build();
    }
}
