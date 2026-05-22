package com.wanted.codebombalms.problems.execution.application.port;

public interface RunCodePort {

    CodeRunResult run(String code);

    record CodeRunResult(
            String output,
            String errorMessage,
            Long executionTimeMs,
            Boolean success
    ) {
    }
}
