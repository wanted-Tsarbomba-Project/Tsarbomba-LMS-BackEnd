package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemSetForUpdateBasePort;
import org.springframework.stereotype.Component;

@Component
public class ProblemSetForUpdatePersistenceAdapter implements LoadProblemSetForUpdateBasePort {

    private final SpringDataProblemSetRepository problemSetRepository;

    public ProblemSetForUpdatePersistenceAdapter(
            SpringDataProblemSetRepository problemSetRepository
    ) {
        this.problemSetRepository = problemSetRepository;
    }

    @Override
    public ProblemSetForUpdateBase loadBase(Long problemSetId) {
        ProblemSetJpaEntity problemSet = problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));

        return new ProblemSetForUpdateBase(
                problemSet.getProblemSetId(),
                problemSet.getTitle(),
                problemSet.getCategory().getCategoryName(),
                problemSet.getDifficulty(),
                problemSet.getDescription()
        );
    }
}
