package com.wanted.codebombalms.submission.presentation;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import com.wanted.codebombalms.submission.application.command.SubmitCodeCommand;
import com.wanted.codebombalms.submission.application.usecase.CodeSubmissionListQueryUseCase;
import com.wanted.codebombalms.submission.application.usecase.CodeSubmissionResultQueryUseCase;
import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase;
import com.wanted.codebombalms.submission.presentation.request.SubmissionRequest;
import com.wanted.codebombalms.submission.presentation.response.CodeSubmissionListResponse;
import com.wanted.codebombalms.submission.presentation.response.CodeSubmissionResultResponse;
import com.wanted.codebombalms.submission.presentation.response.SubmissionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "코드 제출", description = "코드 실행형 문제 제출, 채점 결과 조회, 제출 기록 조회 API")
@RestController
public class SubmissionController {

    private final SubmissionCommandUseCase submissionCommandUseCase;
    private final CodeSubmissionResultQueryUseCase codeSubmissionResultQueryUseCase;
    private final CodeSubmissionListQueryUseCase codeSubmissionListQueryUseCase;

    public SubmissionController(
            SubmissionCommandUseCase submissionCommandUseCase,
            CodeSubmissionResultQueryUseCase codeSubmissionResultQueryUseCase,
            CodeSubmissionListQueryUseCase codeSubmissionListQueryUseCase
    ) {
        this.submissionCommandUseCase = submissionCommandUseCase;
        this.codeSubmissionResultQueryUseCase = codeSubmissionResultQueryUseCase;
        this.codeSubmissionListQueryUseCase = codeSubmissionListQueryUseCase;
    }

    @Operation(
            summary = "코드 답안 제출 및 채점",
            description = """
                학생이 작성한 코드 답안을 제출하면 테스트케이스를 실행하고 통과 여부를 기준으로 정답 여부를 계산합니다.

                정답인 경우 문제에 설정된 point 값을 기준으로 포인트 지급 이벤트를 발행합니다.
                포인트 지급은 제출 트랜잭션 커밋 이후 AFTER_COMMIT 이벤트 리스너에서 후속 처리됩니다.

                응답의 earnedPoint는 정답 제출 시 지급 요청된 포인트 값입니다.
                pointGranted는 포인트 지급 이벤트가 발행되었는지 여부를 의미합니다.
                실제 누적 포인트 반영은 이벤트 처리 후 user_point와 point_history에서 확인할 수 있습니다.
                """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "제출 및 채점 성공. 정답이면 포인트 지급 이벤트가 발행됩니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "정답 제출 성공 및 포인트 지급 요청",
                                    value = """
                                {
                                  "timestamp": "2026-05-27T12:00:00",
                                  "status": 200,
                                  "code": "COMMON-SUCCESS",
                                  "message": "요청이 성공적으로 처리되었습니다.",
                                  "data": {
                                    "submissionId": 3001,
                                    "problemId": 3001,
                                    "isCorrect": true,
                                    "passedTestCount": 2,
                                    "totalTestCount": 2,
                                    "executionStatus": "SUCCESS",
                                    "errorMessage": null,
                                    "attemptNo": 1,
                                    "remainingAttemptCount": 2,
                                    "canRetry": true,
                                    "nextProblemId": 3002,
                                    "isProblemSetCompleted": false,
                                    "earnedPoint": 10,
                                    "pointGranted": true,
                                    "explanation": null
                                  }
                                }
                                """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "PRB-SUB-004 - 코드 입력값 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "코드 필수값 누락",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 400,
                                              "code": "PRB-SUB-004",
                                              "message": "코드 입력값이 올바르지 않습니다.",
                                              "path": "/api/v1/problems/3001/submissions"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "AUTH_TOKEN_EXPIRED - 인증 토큰 만료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "인증 토큰 만료",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 401,
                                              "code": "AUTH_TOKEN_EXPIRED",
                                              "message": "인증 토큰이 만료되었습니다.",
                                              "path": "/api/v1/problems/3001/submissions"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PRB-PBL-001 - 문제를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "문제 없음",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 404,
                                              "code": "PRB-PBL-001",
                                              "message": "문제를 찾을 수 없습니다.",
                                              "path": "/api/v1/problems/9999/submissions"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "PRB-PBL-003 - 이전 문제를 먼저 풀어야 함",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "이전 문제 미해결",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 409,
                                              "code": "PRB-PBL-003",
                                              "message": "이전 문제를 먼저 풀어야 합니다.",
                                              "path": "/api/v1/problems/3002/submissions"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "PRB-SUB-003 - 제출 횟수 초과",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "제출 횟수 초과",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 429,
                                              "code": "PRB-SUB-003",
                                              "message": "제출 횟수를 초과했습니다.",
                                              "path": "/api/v1/problems/3001/submissions"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "PRB-SUB-005 - 코드 채점 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "채점 실패",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 500,
                                              "code": "PRB-SUB-005",
                                              "message": "코드 채점에 실패했습니다.",
                                              "path": "/api/v1/problems/3001/submissions"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/api/v1/problems/{problemId}/submissions")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitCode(
            @Parameter(description = "제출할 코드 문제 ID", example = "3001")
            @PathVariable Long problemId,
            @RequestBody SubmissionRequest request
    ) {
        var command = new SubmitCodeCommand(
                request.getUserId(),
                request.getCode()
        );
        var response = new SubmissionResponse(
                submissionCommandUseCase.handle(problemId, command)
        );

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }

    @Operation(
            summary = "코드 채점 결과 조회",
            description = "학생이 제출한 코드 답안의 채점 결과와 테스트케이스별 통과 정보를 조회합니다. "
                    + "히든 테스트케이스는 통과 여부만 제공하고 actualOutput, errorMessage, executionTimeMs는 null로 반환합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "채점 결과 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "히든 테스트케이스 포함 결과",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 200,
                                              "code": "COMMON-SUCCESS",
                                              "message": "코드 채점 결과 조회 성공",
                                              "data": {
                                                "submissionId": 3001,
                                                "problemId": 3001,
                                                "isCorrect": true,
                                                "passedTestCount": 2,
                                                "totalTestCount": 2,
                                                "executionStatus": "SUCCESS",
                                                "errorMessage": null,
                                                "submittedAt": "2026-05-27T11:30:00",
                                                "testCaseResults": [
                                                  {
                                                    "testCaseId": 3001,
                                                    "isPassed": true,
                                                    "isHidden": false,
                                                    "actualOutput": "(100, 5)",
                                                    "errorMessage": null,
                                                    "executionTimeMs": 120
                                                  },
                                                  {
                                                    "testCaseId": 3002,
                                                    "isPassed": true,
                                                    "isHidden": true,
                                                    "actualOutput": null,
                                                    "errorMessage": null,
                                                    "executionTimeMs": null
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PRB-SUB-001 - 제출 기록 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "제출 기록 없음",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 404,
                                              "code": "PRB-SUB-001",
                                              "message": "제출 기록을 찾을 수 없습니다.",
                                              "path": "/api/v1/submissions/9999/result"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping({
            "/api/v1/code-submissions/{submissionId}",
            "/api/v1/submissions/{submissionId}/result"
    })
    public ResponseEntity<ApiResponse<CodeSubmissionResultResponse>> getCodeSubmissionResult(
            @Parameter(description = "조회할 제출 ID", example = "3001")
            @PathVariable Long submissionId
    ) {
        var response = new CodeSubmissionResultResponse(
                codeSubmissionResultQueryUseCase.handle(submissionId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "코드 채점 결과 조회 성공",
                response
        ));
    }

    @Operation(
            summary = "코드 제출 기록 조회",
            description = "특정 코드 문제에 대한 제출 기록을 페이지 단위로 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "제출 기록 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PRB-PBL-001 - 문제를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "문제 없음",
                                    value = """
                                            {
                                              "timestamp": "2026-05-27T12:00:00",
                                              "status": 404,
                                              "code": "PRB-PBL-001",
                                              "message": "문제를 찾을 수 없습니다.",
                                              "path": "/api/v1/code-problems/9999/submissions"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/api/v1/code-problems/{problemId}/submissions")
    public ResponseEntity<ApiResponse<CodeSubmissionListResponse>> getCodeSubmissions(
            @Parameter(description = "제출 기록을 조회할 코드 문제 ID", example = "3001")
            @PathVariable Long problemId,
            @Parameter(description = "페이지 번호. 기본값 1", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기. 기본값 10", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        var response = new CodeSubmissionListResponse(
                codeSubmissionListQueryUseCase.handle(problemId, page, size)
        );

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "코드 제출 기록 조회 성공",
                response
        ));
    }
}
