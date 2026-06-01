package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemSetEntryPort;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemSetEntryPersistenceAdapter implements LoadProblemSetEntryPort {

    private final SpringDataProblemSetRepository problemSetRepository;

    @Override
    public ProblemSetEntry loadProblemSetEntry(Long problemSetId) {
        ProblemSetJpaEntity problemSet = loadProblemSet(problemSetId);

        return ProblemSetMapper.toEntry(problemSet);
    }

    private ProblemSetJpaEntity loadProblemSet(Long problemSetId) {
        return problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));
    }
}
