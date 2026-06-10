package com.wanted.codebombalms.problems.execution.application.port;

public interface RunCodePort {

    CodeRunResult run(CodeRunCommand command);

    record CodeRunCommand(
            String code,
            String datasetAccessUrl,
            Integer timeoutMs
    ) {
    }

    record CodeRunResult(
            String output,
            String errorMessage,
            Long executionTimeMs,
            Boolean success
    ) {
    }
}
