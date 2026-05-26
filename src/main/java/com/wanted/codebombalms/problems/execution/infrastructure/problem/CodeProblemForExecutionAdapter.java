package com.wanted.codebombalms.problems.execution.infrastructure.problem;

import com.wanted.codebombalms.problems.dataset.infrastructure.persistence.SpringDataProblemDatasetRepository;
import com.wanted.codebombalms.problems.execution.application.port.LoadCodeProblemPort;
import com.wanted.codebombalms.problems.problem.application.service.ProblemQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodeProblemForExecutionAdapter implements LoadCodeProblemPort {

    private static final String ACTIVE = "ACTIVE";
    private final ProblemQueryService problemQueryService;
    private final SpringDataProblemDatasetRepository datasetRepository;

    @Override
    public CodeProblemForExecution loadCodeProblem(Long problemId) {
        var problem = problemQueryService.findProblem(problemId);
        var dataset = datasetRepository
                .findFirstByProblem_ProblemIdAndStatusOrderByDatasetIdDesc(problemId, ACTIVE)
                .orElse(null);

        return new CodeProblemForExecution(
                problem.getProblemId(),
                problem.getProblemType(),
                dataset == null ? null : dataset.getDatasetId(),
                dataset == null ? null : dataset.getOriginalFileName(),
                dataset == null ? null : dataset.getFilePath()
        );
    }
}

