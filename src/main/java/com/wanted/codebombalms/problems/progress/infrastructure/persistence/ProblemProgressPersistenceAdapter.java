package com.wanted.codebombalms.problems.progress.infrastructure.persistence;

import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.ProblemJpaEntity;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.SpringDataProblemRepository;
import com.wanted.codebombalms.problems.progress.domain.model.ProblemProgressItem;
import com.wanted.codebombalms.problems.progress.application.port.CheckProgressProblemSetPort;
import com.wanted.codebombalms.problems.progress.application.port.LoadCurrentProgressPort;
import com.wanted.codebombalms.problems.progress.application.port.LoadProgressProblemPort;
import com.wanted.codebombalms.problems.progress.application.port.ProgressManagementPort;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.ProblemSetJpaEntity;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.SpringDataProblemSetRepository;
import com.wanted.codebombalms.submission.infrastructure.persistence.SpringDataSubmissionRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProblemProgressPersistenceAdapter implements
        CheckProgressProblemSetPort,
        LoadCurrentProgressPort,
        LoadProgressProblemPort,
        ProgressManagementPort {

    private final SpringDataProblemSetRepository problemSetRepository;
    private final SpringDataProgressRepository progressRepository;
    private final SpringDataProblemRepository problemRepository;
    private final SpringDataSubmissionRepository submissionRepository;

    @Override
    public void checkProblemSetExists(Long problemSetId) {
        if (!problemSetRepository.existsById(problemSetId)) {
            throw new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND);
        }
    }

    @Override
    public Integer loadCurrentProblemNumber(Long userId, Long problemSetId) {
        return progressRepository
                .findByUserIdAndProblemSet_ProblemSetId(userId, problemSetId)
                .map(ProgressJpaEntity::getCurrentProblemNumber)
                .orElse(1);
    }

    @Override
    public List<ProblemProgressItem> loadProgressProblems(
            Long userId,
            Long problemSetId,
            Integer currentProblemNumber
    ) {
        return problemRepository
                .findByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderAsc(problemSetId, "ACTIVE")
                .stream()
                .map(problem -> toProgressItem(userId, currentProblemNumber, problem))
                .toList();
    }

    private ProblemProgressItem toProgressItem(
            Long userId,
            Integer currentProblemNumber,
            ProblemJpaEntity problem
    ) {
        Boolean latestCorrect = submissionRepository
                .findTopByUserIdAndProblem_ProblemIdOrderBySubmittedAtDesc(userId, problem.getProblemId())
                .map(submission -> submission.getCorrect())
                .orElse(null);

        return ProblemProgressItem.of(
                problem.getProblemId(),
                problem.getProblemOrder(),
                currentProblemNumber,
                latestCorrect
        );
    }

    @Override
    public void validateCurrentProblem(Long userId, Long problemSetId, Integer problemOrder) {
        ProgressJpaEntity progress = findOrCreateProgress(userId, problemSetId);

        if (Boolean.TRUE.equals(progress.getCompleted())) {
            throw new ConflictException(ProblemErrorCode.ALREADY_COMPLETED);
        }

        if (!progress.getCurrentProblemNumber().equals(problemOrder)) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_NOT_UNLOCKED);
        }
    }

    @Override
    public Integer openNextProblem(Long userId, Long problemSetId) {
        ProgressJpaEntity progress = findOrCreateProgress(userId, problemSetId);

        progress.openNextProblem();

        return progressRepository.save(progress).getCurrentProblemNumber();
    }

    @Override
    public void completeProblemSet(Long userId, Long problemSetId) {
        ProgressJpaEntity progress = findOrCreateProgress(userId, problemSetId);

        if (Boolean.TRUE.equals(progress.getCompleted())) {
            return;
        }

        ProblemSetJpaEntity problemSet = loadProblemSet(problemSetId);
        progress.complete();
        problemSet.increaseCompletedUserCount();

        progressRepository.save(progress);
    }

    private ProgressJpaEntity findOrCreateProgress(Long userId, Long problemSetId) {
        return progressRepository
                .findByUserIdAndProblemSet_ProblemSetId(userId, problemSetId)
                .orElseGet(() -> {
                    ProblemSetJpaEntity problemSet = loadProblemSet(problemSetId);
                    problemSet.increaseStartedUserCount();
                    return progressRepository.save(new ProgressJpaEntity(userId, problemSet));
                });
    }

    private ProblemSetJpaEntity loadProblemSet(Long problemSetId) {
        return problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));
    }
}
