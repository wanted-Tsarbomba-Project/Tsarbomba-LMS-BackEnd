package com.wanted.codebombalms.problems.execution.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.problems.execution.application.command.ExecuteCodeCommand;
import com.wanted.codebombalms.problems.execution.application.usecase.CodeExecutionUseCase;
import com.wanted.codebombalms.problems.execution.presentation.api.request.CodeExecutionRequest;
import com.wanted.codebombalms.problems.execution.presentation.api.response.CodeExecutionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "코드 실행", description = "학생이 제출 전에 코드를 실행하고 출력 또는 오류를 확인하는 API")
@RestController
public class CodeExecutionController {

    private final CodeExecutionUseCase codeExecutionUseCase;

    public CodeExecutionController(CodeExecutionUseCase codeExecutionUseCase) {
        this.codeExecutionUseCase = codeExecutionUseCase;
    }

    @Operation(
            summary = "코드 실행 요청",
            description = "학생이 작성한 코드를 채점 전에 실행하고 출력값이나 실행 오류를 확인합니다. "
                    + "이 API는 제출 기록을 저장하지 않고, 테스트케이스 통과 여부도 판단하지 않습니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "코드 실행 요청 처리 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "실행 성공",
                                            value = """
                                                    {
                                                      "timestamp": "2026-05-27T12:00:00",
                                                      "status": 200,
                                                      "code": "COMMON-SUCCESS",
                                                      "message": "코드 실행 성공",
                                                      "data": {
                                                        "problemId": 3001,
                                                        "output": "(100, 5)",
                                                        "errorMessage": null,
                                                        "executionTimeMs": 820,
                                                        "isSuccess": true
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "실행은 되었지만 코드 오류 발생",
                                            value = """
                                                    {
                                                      "timestamp": "2026-05-27T12:00:00",
                                                      "status": 200,
                                                      "code": "COMMON-SUCCESS",
                                                      "message": "코드 실행 성공",
                                                      "data": {
                                                        "problemId": 3001,
                                                        "output": null,
                                                        "errorMessage": "NameError: name 'result' is not defined",
                                                        "executionTimeMs": 210,
                                                        "isSuccess": false
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "PRB-EXE-001 - 코드 값이 비어 있음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "코드 값 비어 있음",
                                    value = """
                                {
                                  "timestamp": "2026-05-27T12:00:00",
                                  "status": 400,
                                  "code": "PRB-EXE-001",
                                  "message": "코드 값이 비어 있습니다.",
                                  "path": "/api/v1/code-problems/3001/executions"
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
                                              "path": "/api/v1/code-problems/9999/executions"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "PRB-EXE-002 - 코드 실행 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "코드 실행 서버 오류",
                                    value = """
                                {
                                  "timestamp": "2026-05-27T12:00:00",
                                  "status": 500,
                                  "code": "PRB-EXE-002",
                                  "message": "코드 실행에 실패했습니다.",
                                  "path": "/api/v1/code-problems/3001/executions"
                                }
                                """
                            )
                    )
            )
    })
    @PostMapping("/api/v1/code-problems/{problemId}/executions")
    public ResponseEntity<ApiResponse<CodeExecutionResponse>> executeCode(
            @Parameter(description = "실행할 코드 문제 ID", example = "3001")
            @PathVariable Long problemId,
            @RequestBody CodeExecutionRequest request
    ) {
        var command = new ExecuteCodeCommand(request.getCode());
        var response = new CodeExecutionResponse(
                codeExecutionUseCase.handle(problemId, command)
        );

        return ResponseEntity.ok(ApiResponse.success(
                ApiResponseCode.SUCCESS,
                "코드 실행 성공",
                response
        ));
    }
}
