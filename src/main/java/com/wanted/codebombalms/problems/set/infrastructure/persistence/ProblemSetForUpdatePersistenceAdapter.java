package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.problems.dataset.infrastructure.persistence.ProblemDatasetJpaEntity;
import com.wanted.codebombalms.problems.dataset.infrastructure.persistence.SpringDataProblemDatasetRepository;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.hint.infrastructure.persistence.SpringDataProblemHintRepository;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.ProblemJpaEntity;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.SpringDataProblemRepository;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemSetForUpdatePort;
import com.wanted.codebombalms.problems.set.application.usecase.GetProblemSetForUpdateUseCase.ProblemForUpdateView;
import com.wanted.codebombalms.problems.set.application.usecase.GetProblemSetForUpdateUseCase.ProblemSetForUpdateView;
import org.springframework.stereotype.Component;

@Component
public class ProblemSetForUpdatePersistenceAdapter implements LoadProblemSetForUpdatePort {

    private static final String ACTIVE = "ACTIVE";

    private final SpringDataProblemSetRepository problemSetRepository;
    private final SpringDataProblemRepository problemRepository;
    private final SpringDataProblemHintRepository problemHintRepository;
    private final SpringDataProblemDatasetRepository problemDatasetRepository;

    public ProblemSetForUpdatePersistenceAdapter(
            SpringDataProblemSetRepository problemSetRepository,
            SpringDataProblemRepository problemRepository,
            SpringDataProblemHintRepository problemHintRepository,
            SpringDataProblemDatasetRepository problemDatasetRepository
    ) {
        this.problemSetRepository = problemSetRepository;
        this.problemRepository = problemRepository;
        this.problemHintRepository = problemHintRepository;
        this.problemDatasetRepository = problemDatasetRepository;
    }

    @Override
    public ProblemSetForUpdateView load(Long problemSetId) {
        ProblemSetJpaEntity problemSet = problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));

        ProblemDatasetJpaEntity dataset = problemDatasetRepository
                .findFirstByProblemSet_ProblemSetIdAndStatusOrderByDatasetIdDesc(problemSetId, ACTIVE)
                .orElse(null);

        var problems = problemRepository
                .findByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderAsc(problemSetId, ACTIVE)
                .stream()
                .map(problem -> toProblemView(problem, dataset))
                .toList();

        return new ProblemSetForUpdateView(
                problemSet.getProblemSetId(),
                problemSet.getTitle(),
                problemSet.getCategory().getCategoryName(),
                problemSet.getDifficulty(),
                problemSet.getDescription(),
                dataset == null ? null : dataset.getOriginalFileName(),
                dataset == null ? null : dataset.getDatasetId(),
                dataset == null ? null : dataset.getFileUrl(),
                problems
        );
    }

    private ProblemForUpdateView toProblemView(
            ProblemJpaEntity problem,
            ProblemDatasetJpaEntity dataset
    ) {
        var hint = problemHintRepository
                .findByProblem_ProblemIdOrderByHintOrderAsc(problem.getProblemId())
                .stream()
                .findFirst()
                .orElse(null);

        return new ProblemForUpdateView(
                problem.getProblemId(),
                problem.getTitle(),
                problem.getContent(),
                problem.getPoint(),
                createStartCode(dataset),
                problem.getAnswer(),
                hint == null ? null : hint.getHintId(),
                hint == null ? null : hint.getHintContent(),
                problem.getExplanation()
        );
    }

    private String createStartCode(ProblemDatasetJpaEntity dataset) {
        if (dataset == null || dataset.getFileUrl() == null || dataset.getFileUrl().isBlank()) {
            return null;
        }

        return "import pandas as pd\n\n"
                + "df = pd.read_csv(\"" + dataset.getFileUrl() + "\")";
    }
}
