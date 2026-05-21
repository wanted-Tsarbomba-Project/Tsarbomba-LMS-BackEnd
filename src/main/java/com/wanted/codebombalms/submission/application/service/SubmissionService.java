package com.wanted.codebombalms.submission.application.service;

import com.wanted.codebombalms.submission.application.command.SubmitAnswerCommand;
import com.wanted.codebombalms.submission.application.policy.SubmissionAnswerPolicy;
import com.wanted.codebombalms.submission.application.policy.SubmissionAttemptPolicy;
import com.wanted.codebombalms.submission.application.policy.SubmissionScorePolicy;
import com.wanted.codebombalms.submission.application.port.LoadProblemForSubmissionPort;
import com.wanted.codebombalms.submission.application.port.LoadProblemForSubmissionPort.ProblemForSubmission;
import com.wanted.codebombalms.submission.application.port.ProblemProgressPort;
import com.wanted.codebombalms.submission.application.port.ProblemSetCompletionEventPort;
import com.wanted.codebombalms.submission.application.port.SubmissionCommandPort;
import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase;
import com.wanted.codebombalms.submission.domain.model.TextSubmission;
import com.wanted.codebombalms.submission.exception.SubmissionErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmissionService implements SubmissionCommandUseCase {

    private final SubmissionCommandPort submissionCommandPort;
    private final LoadProblemForSubmissionPort loadProblemForSubmissionPort;
    private final ProblemProgressPort problemProgressPort;
    private final ProblemSetCompletionEventPort problemSetCompletionEventPort;
    private final AnswerGradingService answerGradingService;
    private final SubmissionAttemptPolicy submissionAttemptPolicy;
    private final SubmissionAnswerPolicy submissionAnswerPolicy;
    private final SubmissionScorePolicy submissionScorePolicy;


    public SubmissionService(
            SubmissionCommandPort submissionCommandPort,
            LoadProblemForSubmissionPort loadProblemForSubmissionPort,
            ProblemProgressPort problemProgressPort,
            ProblemSetCompletionEventPort problemSetCompletionEventPort,
            AnswerGradingService answerGradingService,
            SubmissionAttemptPolicy submissionAttemptPolicy,
            SubmissionScorePolicy submissionScorePolicy,
            SubmissionAnswerPolicy submissionAnswerPolicy
    ) {
        this.submissionCommandPort = submissionCommandPort;
        this.loadProblemForSubmissionPort = loadProblemForSubmissionPort;
        this.problemProgressPort = problemProgressPort;
        this.problemSetCompletionEventPort = problemSetCompletionEventPort;
        this.answerGradingService = answerGradingService;
        this.submissionAttemptPolicy = submissionAttemptPolicy;
        this.submissionScorePolicy = submissionScorePolicy;
        this.submissionAnswerPolicy = submissionAnswerPolicy;
    }

    @Override
    @Transactional
    public SubmissionView handle(Long problemId, SubmitAnswerCommand command) {
        submissionAnswerPolicy.validate(command.submittedAnswer());

        ProblemForSubmission problem = loadProblemForSubmissionPort.loadProblem(problemId);
        Long problemSetId = problem.problemSetId();

        problemProgressPort.validateCurrentProblem(
                command.userId(),
                problemSetId,
                problem.problemOrder()
        );

        int previousAttemptCount = submissionCommandPort.countAttempts(command.userId(), problemId);

        submissionAttemptPolicy.validateAttemptLimit(
                problem.attemptLimit(),
                problem.retriable(),
                previousAttemptCount
        );

        boolean isCorrect = answerGradingService.gradeTextAnswer(
                problem.answer(),
                command.submittedAnswer()
        );
        int earnedScore = submissionScorePolicy.calculateEarnedScore(
                isCorrect,
                problem.score()
        );
        int attemptNo = previousAttemptCount + 1;
        int remainingAttemptCount = submissionAttemptPolicy.calculateRemainingAttemptCount(
                problem.attemptLimit(),
                attemptNo
        );
        boolean canRetry = submissionAttemptPolicy.canRetry(
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
            nextProblemId = loadProblemForSubmissionPort
                    .findNextProblemId(problemSetId, problem.problemOrder() + 1)
                    .orElse(null);

            if (nextProblemId == null) {
                isProblemSetCompleted = true;
                problemProgressPort.completeProblemSet(
                        command.userId(),
                        problemSetId
                );
                problemSetCompletionEventPort.publishCompleted(
                        command.userId(),
                        problemSetId
                );
            } else {
                problemProgressPort.openNextProblem(
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
}
