package com.wanted.codebombalms.user.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.user.application.query.StudentProblemSubmissionQuery;
import com.wanted.codebombalms.user.application.usecase.GetStudentProblemSubmissionsUseCase;
import com.wanted.codebombalms.user.presentation.api.dto.response.StudentProblemSubmissionListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User - Admin", description = "관리자 전용 학생 관리 API")
@RestController
@RequestMapping("/api/v1/admin/students")
@RequiredArgsConstructor
public class GetStudentProblemSubmissionsController {

    private final GetStudentProblemSubmissionsUseCase getStudentProblemSubmissionsUseCase;

    @Operation(
            summary = "학생 풀이 문제 조회",
            description = """
                    관리자가 학생 상세 조회 페이지에서 해당 학생의 문제 제출 기록을 조회합니다.

                    problemSetId를 전달하면 특정 문제 세트의 제출 기록만 조회합니다.
                    problemId를 전달하면 특정 문제의 제출 기록만 조회합니다.
                    correctOnly=true이면 정답 제출 기록만 조회합니다.

                    제출 기록이 없는 학생은 404가 아니라 빈 배열로 응답합니다.
                    """
    )
    @GetMapping("/{userId}/problems")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentProblemSubmissionListResponse>> getStudentProblemSubmissions(
            @PathVariable Long userId,
            @RequestParam(required = false) Long problemSetId,
            @RequestParam(required = false) Long problemId,
            @RequestParam(defaultValue = "false") Boolean correctOnly
    ) {
        var query = new StudentProblemSubmissionQuery(
                userId,
                problemSetId,
                problemId,
                correctOnly
        );

        var result = getStudentProblemSubmissionsUseCase.getStudentProblemSubmissions(query);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "학생 풀이 문제 조회에 성공했습니다.",
                StudentProblemSubmissionListResponse.from(result)
        ));
    }
}
