package com.wanted.codebombalms.problems.execution.presentation.api.response;

import com.wanted.codebombalms.problems.execution.application.usecase.CodeExecutionUseCase.CodeExecutionView;
import io.swagger.v3.oas.annotations.media.Schema;

public record CodeExecutionResponse(
        @Schema(description = "문제 ID", example = "3001")
        Long problemId,

        @Schema(description = "코드 실행 표준 출력값. 실행 실패 시 null", example = "(100, 5)", nullable = true)
        String output,

        @Schema(description = "코드 실행 오류 메시지. 실행 성공 시 null", example = "NameError: name 'result' is not defined", nullable = true)
        String errorMessage,

        @Schema(description = "코드 실행 시간(ms)", example = "820")
        Long executionTimeMs,

        @Schema(description = "코드 실행 성공 여부", example = "true")
        Boolean isSuccess
) {

    public CodeExecutionResponse(CodeExecutionView result) {
        this(
                result.problemId(),
                result.output(),
                result.errorMessage(),
                result.executionTimeMs(),
                result.success()
        );
    }
}
