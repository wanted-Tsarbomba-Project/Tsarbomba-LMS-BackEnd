package com.wanted.codebombalms.user.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.user.application.query.StudentProblemSubmissionQuery;
import com.wanted.codebombalms.user.application.usecase.GetStudentProblemSubmissionsUseCase;
import com.wanted.codebombalms.user.presentation.api.response.StudentProblemSubmissionListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User - Admin", description = "관리자 전용 학생 관리 API")
@RestController
@RequestMapping("/api/v1/admin/students")
@RequiredArgsConstructor
public class GetStudentProblemSubmissionsController {

    private final GetStudentProblemSubmissionsUseCase getStudentProblemSubmissionsUseCase;

    @Operation(
            summary = "학생 풀이 문제 조회",
            description = """
                    관리자가 특정 학생의 문제 제출 기록을 조회합니다.

                    problemSetId를 전달하면 특정 문제 세트의 제출 기록만 조회합니다.
                    problemId를 전달하면 특정 문제의 제출 기록만 조회합니다.
                    correctOnly=true이면 정답 제출 기록만 조회합니다.

                    제출 기록이 없는 학생은 404가 아니라 200 OK와 빈 배열로 응답합니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "학생 풀이 문제 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 파라미터"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증이 필요합니다."
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한이 필요합니다."
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "학생 회원을 찾을 수 없습니다."
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    @GetMapping("/{userId}/problems")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentProblemSubmissionListResponse>> getStudentProblems(
            @Parameter(description = "조회할 학생 회원 ID", example = "3")
            @PathVariable Long userId,

            @Parameter(description = "문제 세트 ID 필터", example = "3001")
            @RequestParam(required = false) Long problemSetId,

            @Parameter(description = "문제 ID 필터", example = "3001")
            @RequestParam(required = false) Long problemId,

            @Parameter(description = "정답 제출 기록만 조회할지 여부", example = "false")
            @RequestParam(defaultValue = "false") Boolean correctOnly,

            @Parameter(description = "페이지 번호. 0부터 시작합니다.", example = "0")
            @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "페이지 크기. 최대 100개까지 조회합니다.", example = "20")
            @RequestParam(defaultValue = "20") Integer size
    ) {
        var query = new StudentProblemSubmissionQuery(
                userId,
                problemSetId,
                problemId,
                correctOnly,
                page,
                size
        );

        var result = getStudentProblemSubmissionsUseCase.getStudentProblemSubmissions(query);

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "학생 풀이 문제 조회에 성공했습니다.",
                StudentProblemSubmissionListResponse.from(result)
        ));
    }
}
