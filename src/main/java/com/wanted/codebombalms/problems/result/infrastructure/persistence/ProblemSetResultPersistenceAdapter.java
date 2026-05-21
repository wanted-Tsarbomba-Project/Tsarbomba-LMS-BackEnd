package com.wanted.codebombalms.problems.result.infrastructure.persistence;

import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.ProblemJpaEntity;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.SpringDataProblemRepository;
import com.wanted.codebombalms.problems.progress.infrastructure.persistence.SpringDataProgressRepository;
import com.wanted.codebombalms.problems.result.domain.model.ProblemSetResult;
import com.wanted.codebombalms.problems.result.domain.model.ProblemSubmissionResult;
import com.wanted.codebombalms.problems.result.application.port.CheckProblemSetCompletionPort;
import com.wanted.codebombalms.problems.result.application.port.LoadProblemSetResultPort;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.ProblemSetJpaEntity;
import com.wanted.codebombalms.problems.set.infrastructure.persistence.SpringDataProblemSetRepository;
import com.wanted.codebombalms.submission.infrastructure.persistence.SpringDataSubmissionRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemSetResultPersistenceAdapter implements
        CheckProblemSetCompletionPort,
        LoadProblemSetResultPort {

    private final SpringDataProblemSetRepository problemSetRepository;
    private final SpringDataProgressRepository progressRepository;
    private final SpringDataProblemRepository problemRepository;
    private final SpringDataSubmissionRepository submissionRepository;

    @Override
    public void checkCompleted(Long userId, Long problemSetId) {
        problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));

        boolean completed = progressRepository
                .findByUserIdAndProblemSet_ProblemSetId(userId, problemSetId)
                .filter(progress -> Boolean.TRUE.equals(progress.getCompleted()))
                .isPresent();

        if (!completed) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_SET_NOT_COMPLETED);
        }
    }

    @Override
    public ProblemSetResult loadResult(Long userId, Long problemSetId) {
        ProblemSetJpaEntity problemSet = problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));

        return ProblemSetResult.completed(
                problemSet.getProblemSetId(),
                problemSet.getTitle(),
                problemSet.getStartedUserCount(),
                problemSet.getCompletedUserCount(),
                problemRepository
                        .findByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderAsc(problemSetId, "ACTIVE")
                        .stream()
                        .map(problem -> toSubmissionResult(userId, problem))
                        .toList()
        );
    }

    private ProblemSubmissionResult toSubmissionResult(Long userId, ProblemJpaEntity problem) {
        return submissionRepository
                .findTopByUserIdAndProblem_ProblemIdOrderBySubmittedAtDesc(userId, problem.getProblemId())
                .map(submission -> ProblemSubmissionResult.of(
                        problem.getProblemId(),
                        problem.getProblemOrder(),
                        problem.getTitle(),
                        problem.getContent(),
                        submission.getSubmittedAnswer(),
                        submission.getCorrect(),
                        submission.getSubmittedAt(),
                        problem.getExplanation()
                ))
                .orElseGet(() -> ProblemSubmissionResult.of(
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
}
