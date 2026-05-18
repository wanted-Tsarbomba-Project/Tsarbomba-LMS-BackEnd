package com.wanted.codebombalms.domain.submission.service;

import com.wanted.codebombalms.domain.problems.problem.entitiy.Problem;
import com.wanted.codebombalms.domain.problems.problem.service.ProblemService;
import com.wanted.codebombalms.domain.problems.progress.service.ProgressService;
import com.wanted.codebombalms.domain.submission.dto.request.SubmissionRequest;
import com.wanted.codebombalms.domain.submission.dto.response.SubmissionResponse;
import com.wanted.codebombalms.domain.submission.entitiy.Submission;
import com.wanted.codebombalms.domain.submission.event.ProblemSetCompletedEvent;
import com.wanted.codebombalms.domain.submission.repository.SubmissionRepository;
import com.wanted.codebombalms.domain.submission.exception.SubmissionErrorCode;
import com.wanted.codebombalms.global.error.exception.ValidationException;
import com.wanted.codebombalms.global.logging.aop.LogBusiness;
import com.wanted.codebombalms.global.logging.aop.LogPerformance;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ProblemService problemService;
    private final ProgressService progressService;
    private final AnswerGradingService answerGradingService;
    private final SubmissionAttemptService submissionAttemptService;
    private final ApplicationEventPublisher eventPublisher;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            ProblemService problemService,
            ProgressService progressService,
            AnswerGradingService answerGradingService,
            SubmissionAttemptService submissionAttemptService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.submissionRepository = submissionRepository;
        this.problemService = problemService;
        this.progressService = progressService;
        this.answerGradingService = answerGradingService;
        this.submissionAttemptService = submissionAttemptService;
        this.eventPublisher = eventPublisher;
    }

    @LogBusiness
    @LogPerformance
    @Transactional
    public SubmissionResponse submitAnswer(Long problemId, SubmissionRequest request) {
        if (request.getSubmittedAnswer() == null || request.getSubmittedAnswer().isEmpty()) {
            throw new ValidationException(SubmissionErrorCode.INVALID_ANSWER);
        }

        Problem problem = problemService.findProblemEntity(problemId);
        progressService.validateCurrentProblem(
                request.getUserId(),
                problem.getProblemSet(),
                problem.getProblemOrder()
        );

        int previousAttemptCount = submissionRepository
                .countByUserIdAndProblem_ProblemId(request.getUserId(), problemId);

        submissionAttemptService.validateAttemptLimit(problem, previousAttemptCount);

        boolean isCorrect = answerGradingService.gradeTextAnswer(
                problem.getAnswer(),
                request.getSubmittedAnswer()
        );
        int earnedScore = isCorrect ? problem.getScore() : 0;
        int attemptNo = previousAttemptCount + 1;
        int remainingAttemptCount = submissionAttemptService.calculateRemainingAttemptCount(
                problem.getAttemptLimit(),
                attemptNo
        );
        boolean canRetry = submissionAttemptService.canRetry(
                problem.getRetriable(),
                remainingAttemptCount,
                isCorrect
        );

        Submission submission = new Submission(
                request.getUserId(),
                problem,
                request.getSubmittedAnswer(),
                isCorrect,
                earnedScore,
                attemptNo
        );
        submissionRepository.save(submission);

        Long nextProblemId = null;
        boolean isProblemSetCompleted = false;

        if (isCorrect) {
            nextProblemId = problemService
                    .findProblemIdByProblemSetAndOrder(
                            problem.getProblemSet().getProblemSetId(),
                            problem.getProblemOrder() + 1
                    )
                    .orElse(null);

            if (nextProblemId == null) {
                isProblemSetCompleted = true;
                progressService.completeProblemSet(
                        request.getUserId(),
                        problem.getProblemSet()
                );
                eventPublisher.publishEvent(new ProblemSetCompletedEvent(
                        request.getUserId(),
                        problem.getProblemSet().getProblemSetId()
                ));
            } else {
                progressService.openNextProblem(
                        request.getUserId(),
                        problem.getProblemSet()
                );
            }
        }

        return new SubmissionResponse(
                problem.getProblemId(),
                isCorrect,
                attemptNo,
                remainingAttemptCount,
                canRetry,
                nextProblemId,
                isProblemSetCompleted,
                isCorrect ? problem.getExplanation() : null
        );
    }
}
