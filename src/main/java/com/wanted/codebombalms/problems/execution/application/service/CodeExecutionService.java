package com.wanted.codebombalms.problems.execution.application.service;

import com.wanted.codebombalms.problems.execution.application.command.ExecuteCodeCommand;
import com.wanted.codebombalms.problems.execution.application.policy.CodeExecutionPolicy;
import com.wanted.codebombalms.problems.execution.application.port.LoadCodeProblemPort;
import com.wanted.codebombalms.problems.execution.application.port.RunCodePort;
import com.wanted.codebombalms.problems.execution.application.usecase.CodeExecutionUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeExecutionService implements CodeExecutionUseCase {

    private final LoadCodeProblemPort loadCodeProblemPort;
    private final RunCodePort runCodePort;
    private final CodeExecutionPolicy codeExecutionPolicy;

    public CodeExecutionService(
            LoadCodeProblemPort loadCodeProblemPort,
            RunCodePort runCodePort,
            CodeExecutionPolicy codeExecutionPolicy
    ) {
        this.loadCodeProblemPort = loadCodeProblemPort;
        this.runCodePort = runCodePort;
        this.codeExecutionPolicy = codeExecutionPolicy;
    }

    @Override
    @Transactional(readOnly = true)
    public CodeExecutionView handle(Long problemId, ExecuteCodeCommand command) {
        codeExecutionPolicy.validate(command.code());
        var problem = loadCodeProblemPort.loadCodeProblem(problemId);
        var result = runCodePort.run(command.code());

        return new CodeExecutionView(
                problem.problemId(),
                result.output(),
                result.errorMessage(),
                result.executionTimeMs(),
                result.success()
        );
    }
}
