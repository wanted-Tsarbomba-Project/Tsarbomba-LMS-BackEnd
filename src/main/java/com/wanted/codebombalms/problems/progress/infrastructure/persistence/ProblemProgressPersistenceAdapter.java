package com.wanted.codebombalms.problems.progress.infrastructure.persistence;

import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.progress.application.port.LoadCurrentProgressPort;
import com.wanted.codebombalms.problems.progress.application.port.ProgressManagementPort;
import com.wanted.codebombalms.problems.set.application.port.FindOrCreateProblemSetProgressPort;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetProgressState;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.ProblemSetJpaEntity;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class ProblemProgressPersistenceAdapter implements
        LoadCurrentProgressPort,
        ProgressManagementPort,
        FindOrCreateProblemSetProgressPort {

    private final SpringDataProgressRepository progressRepository;
    private final EntityManager entityManager;


    @Override
    public Integer loadCurrentProblemNumber(Long userId, Long problemSetId) {
        return progressRepository
                .findByUserIdAndProblemSet_ProblemSetId(userId, problemSetId)
                .map(ProgressJpaEntity::getCurrentProblemNumber)
                .orElse(1);
    }


    @Override
    public void validateCurrentProblem(Long userId, Long problemSetId, Integer problemOrder) {
        ProgressJpaEntity progress = findOrCreateProgressEntity(userId, problemSetId);

        if (Boolean.TRUE.equals(progress.getCompleted())) {
            throw new ConflictException(ProblemErrorCode.ALREADY_COMPLETED);
        }

        if (!progress.getCurrentProblemNumber().equals(problemOrder)) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_NOT_UNLOCKED);
        }
    }

    @Override
    public ProblemSetProgressState findOrCreateProgress(Long userId, Long problemSetId) {
        return progressRepository
                .findByUserIdAndProblemSet_ProblemSetId(userId, problemSetId)
                .map(progress -> new ProblemSetProgressState(
                        progress.getCurrentProblemNumber(),
                        progress.getCompleted(),
                        false
                ))
                .orElseGet(() -> createProgressStateSafely(userId, problemSetId));
    }

    private ProblemSetProgressState createProgressStateSafely(Long userId, Long problemSetId) {
        try {
            ProblemSetJpaEntity problemSet = getProblemSetReference(problemSetId);
            ProgressJpaEntity saved = progressRepository.saveAndFlush(
                    new ProgressJpaEntity(userId, problemSet)
            );

            return new ProblemSetProgressState(
                    saved.getCurrentProblemNumber(),
                    saved.getCompleted(),
                    true
            );
        } catch (DataIntegrityViolationException e) {
            ProgressJpaEntity progress = progressRepository
                    .findByUserIdAndProblemSet_ProblemSetId(userId, problemSetId)
                    .orElseThrow(() -> e);

            return new ProblemSetProgressState(
                    progress.getCurrentProblemNumber(),
                    progress.getCompleted(),
                    false
            );
        }
    }

    @Override
    public Integer openNextProblem(Long userId, Long problemSetId) {
        ProgressJpaEntity progress = findOrCreateProgressEntity(userId, problemSetId);

        progress.openNextProblem();

        return progressRepository.save(progress).getCurrentProblemNumber();
    }

    @Override
    public void completeProblemSet(Long userId, Long problemSetId) {
        ProgressJpaEntity progress = findOrCreateProgressEntity(userId, problemSetId);

        if (Boolean.TRUE.equals(progress.getCompleted())) {
            return;
        }

        progress.complete();

        progressRepository.save(progress);
    }

    private ProgressJpaEntity findOrCreateProgressEntity(Long userId, Long problemSetId) {
        return progressRepository
                .findByUserIdAndProblemSet_ProblemSetId(userId, problemSetId)
                .orElseGet(() -> {
                    ProblemSetJpaEntity problemSet = getProblemSetReference(problemSetId);
                    return createProgressSafely(userId, problemSet);
                });
    }

    private ProgressJpaEntity createProgressSafely(Long userId, ProblemSetJpaEntity problemSet) {
        try {
            return progressRepository.saveAndFlush(new ProgressJpaEntity(userId, problemSet));
        } catch (DataIntegrityViolationException e) {
            return progressRepository
                    .findByUserIdAndProblemSet_ProblemSetId(userId, problemSet.getProblemSetId())
                    .orElseThrow(() -> e);
        }
    }

    private ProblemSetJpaEntity getProblemSetReference(Long problemSetId) {
        return entityManager.getReference(ProblemSetJpaEntity.class, problemSetId);
    }
}

