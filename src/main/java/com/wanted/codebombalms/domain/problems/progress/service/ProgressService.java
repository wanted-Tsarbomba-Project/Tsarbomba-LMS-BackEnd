package com.wanted.codebombalms.domain.problems.progress.service;

import com.wanted.codebombalms.domain.problems.problem.entitiy.Problem;
import com.wanted.codebombalms.domain.problems.problem.service.ProblemService;
import com.wanted.codebombalms.domain.problems.progress.dto.response.ProblemProgressItemResponse;
import com.wanted.codebombalms.domain.problems.progress.dto.response.ProblemProgressResponse;
import com.wanted.codebombalms.domain.problems.progress.entitiy.Progress;
import com.wanted.codebombalms.domain.problems.progress.enums.ProblemProgressStatus;
import com.wanted.codebombalms.domain.problems.progress.repository.ProgressRepository;
import com.wanted.codebombalms.domain.problems.set.entity.ProblemSet;
import com.wanted.codebombalms.domain.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.domain.problems.set.repository.ProblemSetRepository;
import com.wanted.codebombalms.domain.submission.dto.response.LatestSubmissionResult;
import com.wanted.codebombalms.domain.submission.service.SubmissionQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProgressService {
    private final ProgressRepository problemProgressRepository;
    private final ProblemSetRepository problemSetRepository;
    private final ProblemService problemService;
    private final SubmissionQueryService submissionQueryService;

    public ProgressService(
            ProgressRepository problemProgressRepository,
            ProblemSetRepository problemSetRepository,
            ProblemService problemService,
            SubmissionQueryService submissionQueryService
    ) {
        this.problemProgressRepository = problemProgressRepository;
        this.problemSetRepository = problemSetRepository;
        this.problemService = problemService;
        this.submissionQueryService = submissionQueryService;
    }

    public Integer findCurrentProblemNumber(Long userId, Long problemSetId) {
        return problemProgressRepository
                .findByUserIdAndProblemSet_ProblemSetId(userId, problemSetId)
                .map(Progress::getCurrentProblemNumber)
                .orElse(1);
    }

    @Transactional
    public Integer findOrCreateCurrentProblemNumber(Long userId, ProblemSet problemSet) {
        Progress progress = findOrCreateProgress(userId, problemSet);

        return progress.getCurrentProblemNumber();
    }

    @Transactional
    public Boolean isCompleted(Long userId, ProblemSet problemSet) {
        Progress progress = findOrCreateProgress(userId, problemSet);

        return progress.getCompleted();
    }

    @Transactional
    public void validateCurrentProblem(Long userId, ProblemSet problemSet, Integer problemOrder) {
        Progress progress = findOrCreateProgress(userId, problemSet);

        if (Boolean.TRUE.equals(progress.getCompleted())) {
            throw new ConflictException(ProblemErrorCode.ALREADY_COMPLETED);
        }

        if (!progress.getCurrentProblemNumber().equals(problemOrder)) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_NOT_UNLOCKED);
        }
    }

    @Transactional
    public Integer openNextProblem(Long userId, ProblemSet problemSet) {
        Progress progress = findOrCreateProgress(userId, problemSet);

        progress.openNextProblem();
        Progress savedProgress = problemProgressRepository.save(progress);

        return savedProgress.getCurrentProblemNumber();
    }

    @Transactional
    public void completeProblemSet(Long userId, ProblemSet problemSet) {
        Progress progress = findOrCreateProgress(userId, problemSet);

        if (Boolean.TRUE.equals(progress.getCompleted())) {
            return;
        }

        progress.complete();
        problemSet.increaseCompletedUserCount();

        problemProgressRepository.save(progress);
    }

    @Transactional(readOnly = true)
    public ProblemProgressResponse findProblemSetProgress(Long problemSetId, Long userId) {
        ProblemSet problemSet = problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));

        Integer currentProblemNumber = findCurrentProblemNumber(userId, problemSetId);
        List<Problem> problems = problemService.findActiveProblemEntitiesByProblemSet(problemSetId);

        Long currentProblemId = problems.stream()
                .filter(problem -> problem.getProblemOrder().equals(currentProblemNumber))
                .map(Problem::getProblemId)
                .findFirst()
                .orElse(null);

        List<ProblemProgressItemResponse> progressItems = problems.stream()
                .map(problem -> toProgressItem(userId, currentProblemNumber, problem))
                .toList();

        int solvedProblemCount = (int) progressItems.stream()
                .filter(item -> item.status() == ProblemProgressStatus.CORRECT)
                .count();

        return new ProblemProgressResponse(
                problemSet.getProblemSetId(),
                problems.size(),
                currentProblemNumber,
                currentProblemId,
                solvedProblemCount,
                progressItems
        );
    }

    private Progress findOrCreateProgress(Long userId, ProblemSet problemSet) {
        return problemProgressRepository
                .findByUserIdAndProblemSet_ProblemSetId(userId, problemSet.getProblemSetId())
                .orElseGet(() -> {
                    problemSet.increaseStartedUserCount();
                    return problemProgressRepository.save(new Progress(userId, problemSet));
                });
    }

    private ProblemProgressItemResponse toProgressItem(
            Long userId,
            Integer currentProblemNumber,
            Problem problem
    ) {
        ProblemProgressStatus status = findProblemStatus(userId, currentProblemNumber, problem);

        return new ProblemProgressItemResponse(
                problem.getProblemId(),
                problem.getProblemOrder(),
                status
        );
    }

    private ProblemProgressStatus findProblemStatus(
            Long userId,
            Integer currentProblemNumber,
            Problem problem
    ) {
        if (problem.getProblemOrder() > currentProblemNumber) {
            return ProblemProgressStatus.LOCKED;
        }

        Optional<LatestSubmissionResult> latestSubmission =
                submissionQueryService.findLatestResult(userId, problem.getProblemId());

        if (latestSubmission.isEmpty()) {
            return ProblemProgressStatus.UNSOLVED;
        }

        return Boolean.TRUE.equals(latestSubmission.get().isCorrect())
                ? ProblemProgressStatus.CORRECT
                : ProblemProgressStatus.WRONG;
    }
}
