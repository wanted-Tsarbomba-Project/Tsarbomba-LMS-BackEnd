package com.wanted.codebombalms.course.presentation.api;

import com.wanted.codebombalms.course.presentation.api.request.CourseProblemSetConfigureRequest;
import com.wanted.codebombalms.course.presentation.api.response.CourseProblemSetResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.lecture.application.usecase.LectureProblemSetCommandUseCase;
import com.wanted.codebombalms.lecture.application.usecase.LectureProblemSetQueryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "강좌 문제세트", description = "강좌와 강의에 연결된 문제세트 API")
public class CourseProblemController {

    private static final Logger log = LoggerFactory.getLogger(CourseProblemController.class);

    private final LectureProblemSetCommandUseCase lectureProblemSetCommandUseCase;
    private final LectureProblemSetQueryUseCase lectureProblemSetQueryUseCase;

    @GetMapping("/courses/{courseId}/problem-sets")
    @Operation(summary = "강좌 문제세트 목록 조회")
    public ResponseEntity<ApiResponse<?>> findProblemSetsByCourse(@PathVariable Long courseId) {
        log.info("[CourseProblemController] find course problem sets - courseId: {}", courseId);

        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.RETRIEVED,
                CourseResponseMessage.RETRIEVED,
                lectureProblemSetQueryUseCase.findProblemSetsByCourse(courseId)
                        .stream()
                        .map(CourseProblemSetResponse::from)
                        .toList()
        ));
    }

    @GetMapping("/lectures/{lectureId}/problem-sets")
    @Operation(summary = "강의 문제세트 목록 조회")
    public ResponseEntity<ApiResponse<?>> findProblemSetsByLecture(@PathVariable Long lectureId) {
        log.info("[CourseProblemController] find lecture problem sets - lectureId: {}", lectureId);

        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.RETRIEVED,
                CourseResponseMessage.RETRIEVED,
                lectureProblemSetQueryUseCase.findProblemSetsByLecture(lectureId)
                        .stream()
                        .map(CourseProblemSetResponse::from)
                        .toList()
        ));
    }

    @PutMapping("/courses/{courseId}/problem-sets")
    @Operation(summary = "강좌 문제세트 연결 저장")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<?>> configureProblemSets(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseProblemSetConfigureRequest request
    ) {
        log.info("[CourseProblemController] configure course problem sets - courseId: {}", courseId);

        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.UPDATED,
                CourseResponseMessage.UPDATED,
                lectureProblemSetCommandUseCase.configureProblemSets(request.toCommand(courseId))
                        .stream()
                        .map(CourseProblemSetResponse::from)
                        .toList()
        ));
    }
}
