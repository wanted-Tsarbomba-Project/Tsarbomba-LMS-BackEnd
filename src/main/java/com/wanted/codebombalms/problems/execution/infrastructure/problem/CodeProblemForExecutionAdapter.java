package com.wanted.codebombalms.problems.execution.infrastructure.problem;

import com.wanted.codebombalms.problems.execution.application.port.LoadCodeProblemPort;
import com.wanted.codebombalms.problems.problem.application.service.ProblemQueryService;
import org.springframework.stereotype.Component;

@Component
public class CodeProblemForExecutionAdapter implements LoadCodeProblemPort {

    private final ProblemQueryService problemQueryService;

    public CodeProblemForExecutionAdapter(ProblemQueryService problemQueryService) {
        this.problemQueryService = problemQueryService;
    }

    @Override
    public CodeProblemForExecution loadCodeProblem(Long problemId) {
        var problem = problemQueryService.findProblem(problemId);

        return new CodeProblemForExecution(
                problem.getProblemId(),
                problem.getProblemType()
        );
    }
}
