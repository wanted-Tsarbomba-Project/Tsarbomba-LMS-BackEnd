package com.wanted.codebombalms.problems.execution.application.service;

import com.wanted.codebombalms.problems.dataset.application.port.GenerateDatasetAccessUrlPort;
import com.wanted.codebombalms.problems.execution.application.command.ExecuteCodeCommand;
import com.wanted.codebombalms.problems.execution.application.policy.CodeExecutionPolicy;
import com.wanted.codebombalms.problems.execution.application.port.RunCodePort;
import com.wanted.codebombalms.problems.execution.application.usecase.CodeExecutionUseCase;
import com.wanted.codebombalms.problems.execution.application.port.LoadExecutionDatasetPort;
import com.wanted.codebombalms.problems.execution.application.port.LoadExecutionProblemPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CodeExecutionService implements CodeExecutionUseCase {

    private final LoadExecutionProblemPort loadExecutionProblemPort;
    private final LoadExecutionDatasetPort loadExecutionDatasetPort;
    private final RunCodePort runCodePort;
    private final CodeExecutionPolicy codeExecutionPolicy;
    private final GenerateDatasetAccessUrlPort generateDatasetAccessUrlPort;

    @Override
    @Transactional(readOnly = true)
    public CodeExecutionView handle(Long problemId, ExecuteCodeCommand command) {
        codeExecutionPolicy.validate(command.code());

        var problem = loadExecutionProblemPort.loadProblem(problemId);
        String filePath = loadExecutionDatasetPort
                .loadActiveDatasetFilePath(problem.problemSetId());

        String datasetAccessUrl = filePath == null
                ? null
                : generateDatasetAccessUrlPort.generate(filePath);

        var result = runCodePort.run(new RunCodePort.CodeRunCommand(
                command.code(),
                datasetAccessUrl,
                5000
        ));

        return new CodeExecutionView(
                problem.problemId(),
                result.output(),
                result.errorMessage(),
                result.executionTimeMs(),
                result.success()
        );
    }
}
