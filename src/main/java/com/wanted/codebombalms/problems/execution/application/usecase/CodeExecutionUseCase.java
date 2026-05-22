package com.wanted.codebombalms.problems.execution.application.usecase;

import com.wanted.codebombalms.problems.execution.application.command.ExecuteCodeCommand;

public interface CodeExecutionUseCase {

    CodeExecutionView handle(Long problemId, ExecuteCodeCommand command);

    record CodeExecutionView(
            Long problemId,
            String output,
            String errorMessage,
            Long executionTimeMs,
            Boolean success
    ) {
    }
}
