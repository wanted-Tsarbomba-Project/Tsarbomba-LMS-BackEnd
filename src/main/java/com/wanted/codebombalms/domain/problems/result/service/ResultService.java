package com.wanted.codebombalms.domain.problems.result.service;

import com.wanted.codebombalms.domain.problems.problem.entitiy.Problem;
import com.wanted.codebombalms.domain.problems.problem.service.ProblemService;
import com.wanted.codebombalms.domain.problems.progress.entitiy.Progress;
import com.wanted.codebombalms.domain.problems.progress.repository.ProgressRepository;
import com.wanted.codebombalms.domain.problems.result.dto.response.ProblemSetResultResponse;
import com.wanted.codebombalms.domain.problems.result.dto.response.ProblemSubmissionResultResponse;
import com.wanted.codebombalms.domain.problems.set.entity.ProblemSet;
import com.wanted.codebombalms.domain.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.domain.problems.set.repository.ProblemSetRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.domain.submission.dto.response.LatestSubmissionResult;
import com.wanted.codebombalms.domain.submission.service.SubmissionQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResultService {

    private final ProblemSetRepository problemSetRepository;
    private final ProgressRepository progressRepository;
    private final ProblemService problemService;
    private final SubmissionQueryService submissionQueryService;

    public ResultService(
            ProblemSetRepository problemSetRepository,
            ProgressRepository progressRepository,
            ProblemService problemService,
            SubmissionQueryService submissionQueryService
    ) {
        this.problemSetRepository = problemSetRepository;
        this.progressRepository = progressRepository;
        this.problemService = problemService;
        this.submissionQueryService = submissionQueryService;
    }

    @Transactional(readOnly = true)
    public ProblemSetResultResponse findResult(Long problemSetId, Long userId) {
        ProblemSet problemSet = problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));

        Progress progress = progressRepository
                .findByUserIdAndProblemSet_ProblemSetId(userId, problemSetId)
                .orElseThrow(() -> new ValidationException(ProblemErrorCode.PROBLEM_SET_NOT_COMPLETED));

        if (!Boolean.TRUE.equals(progress.getCompleted())) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_SET_NOT_COMPLETED);
        }

        List<ProblemSubmissionResultResponse> submissions = problemService
                .findActiveProblemEntitiesByProblemSet(problemSetId)
                .stream()
                .map(problem -> findLatestSubmissionResult(userId, problem))
                .toList();

        int startedUserCount = toZeroIfNull(problemSet.getStartedUserCount());
        int completedUserCount = toZeroIfNull(problemSet.getCompletedUserCount());

        return new ProblemSetResultResponse(
                problemSet.getProblemSetId(),
                problemSet.getTitle(),
                true,
                calculateRate(completedUserCount, startedUserCount),
                startedUserCount,
                completedUserCount,
                submissions
        );
    }

    private ProblemSubmissionResultResponse findLatestSubmissionResult(Long userId, Problem problem) {
        return submissionQueryService.findLatestResult(userId, problem.getProblemId())
                .map(latestSubmissionResult -> toProblemSubmissionResultResponse(problem, latestSubmissionResult))
                .orElseGet(() -> new ProblemSubmissionResultResponse(
                        problem.getProblemId(),
                        problem.getProblemOrder(),
                        problem.getTitle(),
                        problem.getContent(),
                        null,
                        false,
                        null,
                        problem.getExplanation()
                ));
    }

    private ProblemSubmissionResultResponse toProblemSubmissionResultResponse(
            Problem problem,
            LatestSubmissionResult latestSubmissionResult
    ) {
        return new ProblemSubmissionResultResponse(
                problem.getProblemId(),
                problem.getProblemOrder(),
                problem.getTitle(),
                problem.getContent(),
                latestSubmissionResult.submittedAnswer(),
                latestSubmissionResult.isCorrect(),
                latestSubmissionResult.submittedAt(),
                problem.getExplanation()
        );
    }

    private Double calculateRate(int completedUserCount, int startedUserCount) {
        if (startedUserCount == 0) {
            return 0.0;
        }

        double rate = completedUserCount * 100.0 / startedUserCount;
        return Math.round(rate * 10) / 10.0;
    }

    private int toZeroIfNull(Integer value) {
        if (value == null) {
            return 0;
        }

        return value;
    }
}
