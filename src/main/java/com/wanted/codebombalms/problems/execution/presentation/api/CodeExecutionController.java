package com.wanted.codebombalms.problems.execution.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponseCode;
import com.wanted.codebombalms.problems.execution.application.command.ExecuteCodeCommand;
import com.wanted.codebombalms.problems.execution.application.usecase.CodeExecutionUseCase;
import com.wanted.codebombalms.problems.execution.presentation.api.request.CodeExecutionRequest;
import com.wanted.codebombalms.problems.execution.presentation.api.response.CodeExecutionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CodeExecutionController {

    private final CodeExecutionUseCase codeExecutionUseCase;

    public CodeExecutionController(CodeExecutionUseCase codeExecutionUseCase) {
        this.codeExecutionUseCase = codeExecutionUseCase;
    }

    @PostMapping("/api/v1/code-problems/{problemId}/executions")
    public ResponseEntity<ApiResponse<CodeExecutionResponse>> executeCode(
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
