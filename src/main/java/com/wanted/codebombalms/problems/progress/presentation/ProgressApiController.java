package com.wanted.codebombalms.problems.progress.presentation;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseMessage;
import com.wanted.codebombalms.problems.progress.application.query.GetProblemProgressQuery;
import com.wanted.codebombalms.problems.progress.application.usecase.GetProblemProgressUseCase;
import com.wanted.codebombalms.problems.progress.presentation.response.ProblemProgressResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "문제 진행 상태", description = "학생의 문제 세트 풀이 진행률, 현재 풀어야 할 문제, 문제별 열림 상태 조회 API")
@RestController
@RequiredArgsConstructor
public class ProgressApiController {

    private final GetProblemProgressUseCase getProblemProgressUseCase;

    @Operation(
            summary = "문제 세트 진행 상태 조회",
            description = "특정 학생이 문제 세트에서 어디까지 풀었는지 조회합니다. "
                    + "응답의 currentProblemId는 현재 이어풀기로 진입할 문제 ID입니다. "
                    + "problems.status는 화면에서 문제를 열어줄지 판단하는 상태값입니다. "
                    + "status는 LOCKED, OPEN, SOLVED 중 하나입니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "문제 세트 진행 상태 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "진행 상태 조회 성공",
                                    value = """
                                        {
                                          "timestamp": "2026-05-27T12:00:00",
                                          "status": 200,
                                          "code": "COMMON-SUCCESS",
                                          "message": "요청이 성공적으로 처리되었습니다.",
                                          "data": {
                                            "problemSetId": 3001,
                                            "totalProblemCount": 3,
                                            "currentProblemNumber": 2,
                                            "currentProblemId": 3002,
                                            "solvedProblemCount": 1,
                                            "problems": [
                                              {
                                                "problemId": 3001,
                                                "problemNumber": 1,
                                                "status": "SOLVED"
                                              },
                                              {
                                                "problemId": 3002,
                                                "problemNumber": 2,
                                                "status": "OPEN"
                                              },
                                              {
                                                "problemId": 3003,
                                                "problemNumber": 3,
                                                "status": "LOCKED"
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
                    description = "PRB-SET-001 - 문제 세트를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "문제 세트 없음",
                                    value = """
                                        {
                                          "timestamp": "2026-05-27T12:00:00",
                                          "status": 404,
                                          "code": "PRB-SET-001",
                                          "message": "문제 세트를 찾을 수 없습니다.",
                                          "path": "/api/v1/problem-sets/9999/progress"
                                        }
                                        """
                            )
                    )
            )
    })
    @GetMapping("/api/v1/problem-sets/{problemSetId}/progress")
    public ResponseEntity<ApiResponse<ProblemProgressResponse>> findProblemSetProgress(
            @Parameter(description = "진행 상태를 조회할 문제 세트 ID", example = "3001")
            @PathVariable Long problemSetId,

            @Parameter(description = "진행 상태를 조회할 학생 ID", example = "3")
            @RequestParam Long userId
    ) {
        var query = new GetProblemProgressQuery(problemSetId, userId);

        var response = new ProblemProgressResponse(
                getProblemProgressUseCase.handle(query)
        );

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                ApiResponseMessage.SUCCESS,
                response
        ));
    }
}
