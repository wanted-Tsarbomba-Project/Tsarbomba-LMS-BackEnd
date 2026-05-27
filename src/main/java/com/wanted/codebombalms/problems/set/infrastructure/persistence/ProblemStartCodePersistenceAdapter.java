package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import com.wanted.codebombalms.problems.dataset.infrastructure.persistence.SpringDataProblemDatasetRepository;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemStartCodePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemStartCodePersistenceAdapter implements LoadProblemStartCodePort {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final SpringDataProblemDatasetRepository problemDatasetRepository;
    private final com.wanted.codebombalms.problems.problem.infrastructure.persistence.SpringDataProblemRepository problemRepository;

    @Override
    public String loadStartCode(Long problemId) {
        return problemRepository.findById(problemId)
                .flatMap(problem -> problemDatasetRepository.findFirstByProblemSet_ProblemSetIdAndStatus(
                        problem.getProblemSet().getProblemSetId(),
                        ACTIVE_STATUS
                ))
                .map(dataset -> "import pandas as pd\n\n"
                        + "df = pd.read_csv(\"" + dataset.getFileUrl() + "\")")
                .orElse(null);
    }
}
