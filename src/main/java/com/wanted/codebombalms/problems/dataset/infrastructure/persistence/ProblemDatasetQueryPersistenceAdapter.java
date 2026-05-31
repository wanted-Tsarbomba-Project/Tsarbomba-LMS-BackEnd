package com.wanted.codebombalms.problems.dataset.infrastructure.persistence;

import com.wanted.codebombalms.problems.dataset.application.port.LoadProblemDatasetPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProblemDatasetQueryPersistenceAdapter implements LoadProblemDatasetPort {

    private final SpringDataProblemDatasetRepository problemDatasetRepository;

    @Override
    public Optional<String> findLatestActiveMetadata(Long problemSetId) {
        return problemDatasetRepository
                .findFirstByProblemSet_ProblemSetIdAndStatusOrderByDatasetIdDesc(problemSetId, "ACTIVE")
                .map(ProblemDatasetJpaEntity::getMetadata);
    }
}
