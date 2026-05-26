package com.wanted.codebombalms.problems.execution.application.service;

import com.wanted.codebombalms.problems.execution.application.command.ExecuteCodeCommand;
import com.wanted.codebombalms.problems.execution.application.policy.CodeExecutionPolicy;
import com.wanted.codebombalms.problems.execution.application.port.LoadCodeProblemPort;
import com.wanted.codebombalms.problems.execution.application.port.RunCodePort;
import com.wanted.codebombalms.problems.execution.application.usecase.CodeExecutionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CodeExecutionService implements CodeExecutionUseCase {

    private final LoadCodeProblemPort loadCodeProblemPort;
    private final RunCodePort runCodePort;
    private final CodeExecutionPolicy codeExecutionPolicy;

    @Override
    @Transactional(readOnly = true)
    public CodeExecutionView handle(Long problemId, ExecuteCodeCommand command) {
        codeExecutionPolicy.validate(command.code());
        var problem = loadCodeProblemPort.loadCodeProblem(problemId);
        codeExecutionPolicy.validateDatasetAccess(problem.datasetPath());
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
