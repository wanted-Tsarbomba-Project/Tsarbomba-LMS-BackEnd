package com.wanted.codebombalms.learning.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.learning.application.command.RecordLectureProblemProgressCommand;
import com.wanted.codebombalms.learning.application.command.RecordLectureProgressCommand;
import com.wanted.codebombalms.learning.application.usecase.AdminLearningProgressQueryUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProblemProgressCommandUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProblemSetQueryUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProblemSubmissionUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProgressCommandUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProgressQueryUseCase;
import com.wanted.codebombalms.learning.presentation.api.request.LectureProblemProgressRequest;
import com.wanted.codebombalms.learning.presentation.api.request.LectureProgressRequest;
import com.wanted.codebombalms.learning.presentation.api.response.CourseLearningProgressResponse;
import com.wanted.codebombalms.learning.presentation.api.response.LearningProgressSummaryResponse;
import com.wanted.codebombalms.learning.presentation.api.response.LectureLearningProgressResponse;
import com.wanted.codebombalms.learning.presentation.api.response.LectureProblemProgressResponse;
import com.wanted.codebombalms.learning.presentation.api.response.LectureProblemSetEntryResponse;
import com.wanted.codebombalms.learning.presentation.api.response.LectureProblemSetProgressResponse;
import com.wanted.codebombalms.learning.presentation.api.response.LectureProblemStatisticsResponse;
import com.wanted.codebombalms.learning.presentation.api.response.LectureProgressResponse;
import com.wanted.codebombalms.learning.presentation.api.response.StudentLearningProgressResponse;
import com.wanted.codebombalms.submission.application.command.SubmitCodeCommand;
import com.wanted.codebombalms.submission.presentation.request.SubmissionRequest;
import com.wanted.codebombalms.submission.presentation.response.SubmissionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "학습", description = "학습 진행률 및 강의 문제풀이 API")
public class LearningController {

    private final LectureProgressCommandUseCase lectureProgressCommandUseCase;
    private final LectureProgressQueryUseCase lectureProgressQueryUseCase;
    private final LectureProblemProgressCommandUseCase lectureProblemProgressCommandUseCase;
    private final LectureProblemSetQueryUseCase lectureProblemSetQueryUseCase;
    private final LectureProblemSubmissionUseCase lectureProblemSubmissionUseCase;
    private final AdminLearningProgressQueryUseCase adminLearningProgressQueryUseCase;

    @PatchMapping("/lectures/{lectureId}/progress")
    @Operation(summary = "강의 수강 진행률 저장")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LectureProgressResponse>> recordLectureProgress(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lectureId,
            @Valid @RequestBody LectureProgressRequest request
    ) {
        LectureProgressResponse response = LectureProgressResponse.from(
                lectureProgressCommandUseCase.recordProgress(new RecordLectureProgressCommand(
                        userId,
                        lectureId,
                        request.completed()
                ))
        );

        return ResponseEntity.ok(ApiResponse.success(
                LearningResponseCode.UPDATED,
                LearningResponseMessage.UPDATED,
                response
        ));
    }

    @GetMapping("/lectures/{lectureId}/progress")
    @Operation(summary = "강의 수강 진행률 조회")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LectureProgressResponse>> findLectureProgress(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lectureId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                LearningResponseCode.RETRIEVED,
                LearningResponseMessage.RETRIEVED,
                LectureProgressResponse.from(lectureProgressQueryUseCase.findProgress(userId, lectureId))
        ));
    }

    @GetMapping("/lecture-problem-sets/{lectureProblemSetId}")
    @Operation(summary = "강의 문제세트 입장")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LectureProblemSetEntryResponse>> enterLectureProblemSet(
            @PathVariable Long lectureProblemSetId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                LearningResponseCode.RETRIEVED,
                LearningResponseMessage.RETRIEVED,
                LectureProblemSetEntryResponse.from(
                        lectureProblemSetQueryUseCase.enterLectureProblemSet(userId, lectureProblemSetId)
                )
        ));
    }

    @GetMapping("/lecture-problem-sets/{lectureProblemSetId}/progress")
    @Operation(summary = "강의 문제세트 진행 상태 조회")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LectureProblemSetProgressResponse>> findLectureProblemSetProgress(
            @PathVariable Long lectureProblemSetId,
            @AuthenticationPrincipal Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                LearningResponseCode.RETRIEVED,
                LearningResponseMessage.RETRIEVED,
                LectureProblemSetProgressResponse.from(
                        lectureProblemSetQueryUseCase.findLectureProblemSetProgress(userId, lectureProblemSetId)
                )
        ));
    }

    @PatchMapping("/lecture-problem-sets/{lectureProblemSetId}/progress")
    @Operation(summary = "강의 문제세트 진행 상태 저장")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LectureProblemProgressResponse>> recordLectureProblemSetProgress(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long lectureProblemSetId,
            @Valid @RequestBody LectureProblemProgressRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                LearningResponseCode.UPDATED,
                LearningResponseMessage.UPDATED,
                LectureProblemProgressResponse.from(lectureProblemProgressCommandUseCase.recordProgress(
                        new RecordLectureProblemProgressCommand(
                                userId,
                                lectureProblemSetId,
                                request.currentProblemNumber(),
                                request.completed()
                        )
                ))
        ));
    }
    @PostMapping("/lecture-problem-sets/{lectureProblemSetId}/problems/{problemId}/submissions")
    @Operation(summary = "강의 문제 제출")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitLectureProblem(
            @PathVariable Long lectureProblemSetId,
            @PathVariable Long problemId,
            @AuthenticationPrincipal Long userId,
            @RequestBody SubmissionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                LearningResponseCode.SUBMITTED,
                LearningResponseMessage.SUBMITTED,
                new SubmissionResponse(lectureProblemSubmissionUseCase.submit(
                        lectureProblemSetId,
                        problemId,
                        new SubmitCodeCommand(userId, request.getCode())
                ))
        ));
    }

    @GetMapping("/courses/{courseId}/learning-progress")
    @Operation(summary = "강좌 학습률 조회")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<CourseLearningProgressResponse>> findCourseLearningProgress(
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                LearningResponseCode.RETRIEVED,
                LearningResponseMessage.RETRIEVED,
                CourseLearningProgressResponse.from(adminLearningProgressQueryUseCase.findCourseProgress(courseId))
        ));
    }

    @GetMapping("/courses/learning-progress")
    @Operation(summary = "전체 강좌 학습률 조회")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<List<CourseLearningProgressResponse>>> findCourseLearningProgresses() {
        return ResponseEntity.ok(ApiResponse.success(
                LearningResponseCode.RETRIEVED,
                LearningResponseMessage.RETRIEVED,
                adminLearningProgressQueryUseCase.findCourseProgresses()
                        .stream()
                        .map(CourseLearningProgressResponse::from)
                        .toList()
        ));
    }

    @GetMapping("/courses/{courseId}/users/learning-progress")
    @Operation(summary = "강좌별 학생 학습률 목록 조회")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<List<StudentLearningProgressResponse>>> findStudentLearningProgresses(
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                LearningResponseCode.RETRIEVED,
                LearningResponseMessage.RETRIEVED,
                adminLearningProgressQueryUseCase.findStudentProgresses(courseId)
                        .stream()
                        .map(StudentLearningProgressResponse::from)
                        .toList()
        ));
    }

    @GetMapping("/courses/{courseId}/users/{userId}/learning-progress")
    @Operation(summary = "학생 학습률 단건 조회")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<StudentLearningProgressResponse>> findStudentLearningProgress(
            @PathVariable Long courseId,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                LearningResponseCode.RETRIEVED,
                LearningResponseMessage.RETRIEVED,
                StudentLearningProgressResponse.from(
                        adminLearningProgressQueryUseCase.findStudentProgress(courseId, userId)
                )
        ));
    }

    @GetMapping("/courses/{courseId}/lectures/learning-progress")
    @Operation(summary = "강좌별 강의 학습률 목록 조회")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<List<LectureLearningProgressResponse>>> findLectureLearningProgresses(
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                LearningResponseCode.RETRIEVED,
                LearningResponseMessage.RETRIEVED,
                adminLearningProgressQueryUseCase.findLectureProgresses(courseId)
                        .stream()
                        .map(LectureLearningProgressResponse::from)
                        .toList()
        ));
    }

    @GetMapping("/lectures/{lectureId}/problems/statistics")
    @Operation(summary = "강의 문제 통계 조회")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<LectureProblemStatisticsResponse>> findLectureProblemStatistics(
            @PathVariable Long lectureId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                LearningResponseCode.RETRIEVED,
                LearningResponseMessage.RETRIEVED,
                LectureProblemStatisticsResponse.from(
                        adminLearningProgressQueryUseCase.findLectureProblemStatistics(lectureId)
                )
        ));
    }

    @GetMapping("/learning-progress/summary")
    @Operation(summary = "학습률 요약 조회")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<LearningProgressSummaryResponse>> summarizeLearningProgress() {
        return ResponseEntity.ok(ApiResponse.success(
                LearningResponseCode.RETRIEVED,
                LearningResponseMessage.RETRIEVED,
                LearningProgressSummaryResponse.from(adminLearningProgressQueryUseCase.summarizeLearningProgress())
        ));
    }
}
