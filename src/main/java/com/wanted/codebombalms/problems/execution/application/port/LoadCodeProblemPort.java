package com.wanted.codebombalms.problems.execution.application.port;

public interface LoadCodeProblemPort {

    CodeProblemForExecution loadCodeProblem(Long problemId);

    record CodeProblemForExecution(
            Long problemId,
            String problemType
    ) {
    }
}
