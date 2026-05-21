package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.progress.infrastructure.persistence.ProgressJpaEntity;
import com.wanted.codebombalms.problems.progress.infrastructure.persistence.SpringDataProgressRepository;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetEntry;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetProgressState;
import com.wanted.codebombalms.problems.set.application.port.FindOrCreateProblemSetProgressPort;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemSetEntryPort;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemSetEntryPersistenceAdapter implements
        LoadProblemSetEntryPort,
        FindOrCreateProblemSetProgressPort {

    private final SpringDataProblemSetRepository problemSetRepository;
    private final SpringDataProgressRepository progressRepository;

    @Override
    public ProblemSetEntry loadProblemSetEntry(Long problemSetId) {
        ProblemSetJpaEntity problemSet = loadProblemSet(problemSetId);

        return ProblemSetMapper.toEntry(problemSet);
    }

    @Override
    public ProblemSetProgressState findOrCreateProgress(Long userId, Long problemSetId) {
        ProblemSetJpaEntity problemSet = loadProblemSet(problemSetId);
        ProgressJpaEntity progress = progressRepository
                .findByUserIdAndProblemSet_ProblemSetId(userId, problemSetId)
                .orElseGet(() -> {
                    problemSet.increaseStartedUserCount();
                    return progressRepository.save(new ProgressJpaEntity(userId, problemSet));
                });

        return new ProblemSetProgressState(
                progress.getCurrentProblemNumber(),
                progress.getCompleted()
        );
    }

    private ProblemSetJpaEntity loadProblemSet(Long problemSetId) {
        return problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));
    }
}
