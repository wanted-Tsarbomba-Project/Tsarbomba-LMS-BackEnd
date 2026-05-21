package com.wanted.codebombalms.submission.application.service;

import com.wanted.codebombalms.problems.problem.application.service.ProblemQueryService;
import com.wanted.codebombalms.problems.problem.application.service.ProblemQueryService.ProblemForSubmissionView;
import com.wanted.codebombalms.problems.progress.application.service.ProgressCommandService;
import com.wanted.codebombalms.submission.application.command.SubmitAnswerCommand;
import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase;
import com.wanted.codebombalms.submission.domain.event.ProblemSetCompletedEvent;
import com.wanted.codebombalms.submission.domain.model.TextSubmission;
import com.wanted.codebombalms.submission.application.port.SubmissionCommandPort;
import com.wanted.codebombalms.submission.exception.SubmissionErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmissionService implements SubmissionCommandUseCase {

    private final SubmissionCommandPort submissionCommandPort;
    private final ProblemQueryService problemQueryService;
    private final ProgressCommandService progressCommandService;
    private final AnswerGradingService answerGradingService;
    private final SubmissionAttemptService submissionAttemptService;
    private final ApplicationEventPublisher eventPublisher;

    public SubmissionService(
            SubmissionCommandPort submissionCommandPort,
            ProblemQueryService problemQueryService,
            ProgressCommandService progressCommandService,
            AnswerGradingService answerGradingService,
            SubmissionAttemptService submissionAttemptService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.submissionCommandPort = submissionCommandPort;
        this.problemQueryService = problemQueryService;
        this.progressCommandService = progressCommandService;
        this.answerGradingService = answerGradingService;
        this.submissionAttemptService = submissionAttemptService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public SubmissionView handle(Long problemId, SubmitAnswerCommand command) {
        validateAnswer(command.submittedAnswer());

        ProblemForSubmissionView problem = problemQueryService.findProblemForSubmission(problemId);
        Long problemSetId = problem.problemSetId();

        progressCommandService.validateCurrentProblem(
                command.userId(),
                problemSetId,
                problem.problemOrder()
        );

        int previousAttemptCount = submissionCommandPort.countAttempts(command.userId(), problemId);

        submissionAttemptService.validateAttemptLimit(
                problem.attemptLimit(),
                problem.retriable(),
                previousAttemptCount
        );

        boolean isCorrect = answerGradingService.gradeTextAnswer(
                problem.answer(),
                command.submittedAnswer()
        );
        int earnedScore = isCorrect ? problem.score() : 0;
        int attemptNo = previousAttemptCount + 1;
        int remainingAttemptCount = submissionAttemptService.calculateRemainingAttemptCount(
                problem.attemptLimit(),
                attemptNo
        );
        boolean canRetry = submissionAttemptService.canRetry(
                problem.retriable(),
                remainingAttemptCount,
                isCorrect
        );

        submissionCommandPort.saveTextSubmission(new TextSubmission(
                command.userId(),
                problem.problemId(),
                command.submittedAnswer(),
                isCorrect,
                earnedScore,
                attemptNo
        ));

        Long nextProblemId = null;
        boolean isProblemSetCompleted = false;

        if (isCorrect) {
            nextProblemId = problemQueryService
                    .findProblemIdByProblemSetAndOrder(
                            problemSetId,
                            problem.problemOrder() + 1
                    )
                    .orElse(null);

            if (nextProblemId == null) {
                isProblemSetCompleted = true;
                progressCommandService.completeProblemSet(
                        command.userId(),
                        problemSetId
                );
                eventPublisher.publishEvent(new ProblemSetCompletedEvent(
                        command.userId(),
                        problemSetId
                ));
            } else {
                progressCommandService.openNextProblem(
                        command.userId(),
                        problemSetId
                );
            }
        }

        return new SubmissionView(
                problem.problemId(),
                isCorrect,
                attemptNo,
                remainingAttemptCount,
                canRetry,
                nextProblemId,
                isProblemSetCompleted,
                isCorrect ? problem.explanation() : null
        );
    }

    private void validateAnswer(String submittedAnswer) {
        if (submittedAnswer == null || submittedAnswer.isEmpty()) {
            throw new ValidationException(SubmissionErrorCode.INVALID_ANSWER);
        }
    }
}
