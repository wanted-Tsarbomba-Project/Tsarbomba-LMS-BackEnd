package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import com.wanted.codebombalms.problems.category.application.port.CheckProblemCategoryUsagePort;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProblemCategoryUsagePersistenceAdapter implements CheckProblemCategoryUsagePort {

    private final SpringDataProblemSetRepository problemSetRepository;

    @Override
    public boolean existsActiveProblemSet(Long categoryId) {
        return problemSetRepository.existsByCategory_CategoryIdAndStatus(
                categoryId,
                ProblemSetStatus.ACTIVE
        );
    }
}
