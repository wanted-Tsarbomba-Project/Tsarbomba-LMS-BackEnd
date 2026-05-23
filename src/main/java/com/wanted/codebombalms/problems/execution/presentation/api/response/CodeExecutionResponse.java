package com.wanted.codebombalms.problems.execution.presentation.api.response;

import com.wanted.codebombalms.problems.execution.application.usecase.CodeExecutionUseCase.CodeExecutionView;

public record CodeExecutionResponse(
        Long problemId,
        String output,
        String errorMessage,
        Long executionTimeMs,
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
