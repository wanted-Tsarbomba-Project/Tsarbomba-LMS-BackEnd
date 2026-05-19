package com.wanted.codebombalms.domain.problems.dataset.service;

import com.wanted.codebombalms.domain.problems.dataset.repository.ProblemDatasetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProblemDatasetReader {

    private final ProblemDatasetRepository problemDatasetRepository;

    @Transactional(readOnly = true)
    public String findStartCodeByProblemId(Long problemId) {
        return problemDatasetRepository.findFirstByProblem_ProblemIdAndStatus(problemId, "ACTIVE")
                .map(dataset -> "import pandas as pd\n\n"
                        + "df = pd.read_csv(\"" + dataset.getFilePath() + "\")")
                .orElse(null);
    }
}
