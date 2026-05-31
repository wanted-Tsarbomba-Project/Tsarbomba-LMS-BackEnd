package com.wanted.codebombalms.problems.execution.application.port;

public interface LoadExecutionProblemPort {

    ExecutionProblem loadProblem(Long problemId);

    record ExecutionProblem(
            Long problemId,
            Long problemSetId,
            String problemType
    ) {
    }
}
