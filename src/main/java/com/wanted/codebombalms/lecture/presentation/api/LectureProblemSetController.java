package com.wanted.codebombalms.lecture.presentation.api;

import com.wanted.codebombalms.course.presentation.api.CourseResponseCode;
import com.wanted.codebombalms.course.presentation.api.CourseResponseMessage;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.lecture.application.usecase.LectureProblemSetCommandUseCase;
import com.wanted.codebombalms.lecture.application.usecase.LectureProblemSetQueryUseCase;
import com.wanted.codebombalms.lecture.presentation.api.request.LectureProblemSetConfigureRequest;
import com.wanted.codebombalms.lecture.presentation.api.response.LectureProblemSetResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "강의 문제세트", description = "강의-문제세트 연결 관리 API")
public class LectureProblemSetController {

    private static final Logger log = LoggerFactory.getLogger(LectureProblemSetController.class);

    private final LectureProblemSetCommandUseCase lectureProblemSetCommandUseCase;
    private final LectureProblemSetQueryUseCase lectureProblemSetQueryUseCase;

    @GetMapping("/courses/{courseId}/lecture-problem-sets")
    @Operation(summary = "강좌별 강의 문제세트 목록 조회")
    public ResponseEntity<ApiResponse<?>> findProblemSetsByCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Long userId,
            Authentication authentication
    ) {
        log.info("[LectureProblemSetController] find course lecture problem sets - courseId: {}", courseId);

        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.RETRIEVED,
                CourseResponseMessage.RETRIEVED,
                lectureProblemSetQueryUseCase.findProblemSetsByCourseForAccess(
                                courseId,
                                userId,
                                isOperator(authentication)
                        )
                        .stream()
                        .map(LectureProblemSetResponse::from)
                        .toList()
        ));
    }

    @GetMapping("/lectures/{lectureId}/lecture-problem-sets")
    @Operation(summary = "강의별 강의 문제세트 목록 조회")
    public ResponseEntity<ApiResponse<?>> findProblemSetsByLecture(
            @PathVariable Long lectureId,
            @AuthenticationPrincipal Long userId,
            Authentication authentication
    ) {
        log.info("[LectureProblemSetController] find lecture problem sets - lectureId: {}", lectureId);

        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.RETRIEVED,
                CourseResponseMessage.RETRIEVED,
                lectureProblemSetQueryUseCase.findProblemSetsByLectureForAccess(
                                lectureId,
                                userId,
                                isOperator(authentication)
                        )
                        .stream()
                        .map(LectureProblemSetResponse::from)
                        .toList()
        ));
    }

    @PutMapping("/courses/{courseId}/lecture-problem-sets")
    @Operation(summary = "강좌 강의 문제세트 구성 설정")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<?>> configureProblemSets(
            @PathVariable Long courseId,
            @Valid @RequestBody LectureProblemSetConfigureRequest request
    ) {
        log.info("[LectureProblemSetController] configure course lecture problem sets - courseId: {}", courseId);

        return ResponseEntity.ok(ApiResponse.success(
                CourseResponseCode.UPDATED,
                CourseResponseMessage.UPDATED,
                lectureProblemSetCommandUseCase.configureProblemSets(request.toCommand(courseId))
                        .stream()
                        .map(LectureProblemSetResponse::from)
                        .toList()
        ));
    }

    private boolean isOperator(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_OPERATOR".equals(authority.getAuthority()));
    }
}
