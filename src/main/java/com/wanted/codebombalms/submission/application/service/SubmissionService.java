package com.wanted.codebombalms.submission.application.service;

import com.wanted.codebombalms.submission.application.command.SubmitCodeCommand;
import com.wanted.codebombalms.submission.application.policy.SubmissionAttemptPolicy;
import com.wanted.codebombalms.submission.application.policy.SubmissionCodePolicy;
import com.wanted.codebombalms.submission.application.port.LoadProblemForSubmissionPort;
import com.wanted.codebombalms.submission.application.port.LoadProblemForSubmissionPort.ProblemForSubmission;
import com.wanted.codebombalms.submission.application.port.LoadTestCasesForGradingPort;
import com.wanted.codebombalms.submission.application.port.ProblemProgressPort;
import com.wanted.codebombalms.submission.application.port.ProblemSetCompletionEventPort;
import com.wanted.codebombalms.submission.application.port.SubmissionCommandPort;
import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase;
import com.wanted.codebombalms.submission.domain.model.CodeSubmission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmissionService implements SubmissionCommandUseCase {

    private final SubmissionCommandPort submissionCommandPort;
    private final LoadProblemForSubmissionPort loadProblemForSubmissionPort;
    private final LoadTestCasesForGradingPort loadTestCasesForGradingPort;
    private final ProblemProgressPort problemProgressPort;
    private final ProblemSetCompletionEventPort problemSetCompletionEventPort;
    private final CodeGradingService codeGradingService;
    private final SubmissionAttemptPolicy submissionAttemptPolicy;
    private final SubmissionCodePolicy submissionCodePolicy;

    public SubmissionService(
            SubmissionCommandPort submissionCommandPort,
            LoadProblemForSubmissionPort loadProblemForSubmissionPort,
            LoadTestCasesForGradingPort loadTestCasesForGradingPort,
            ProblemProgressPort problemProgressPort,
            ProblemSetCompletionEventPort problemSetCompletionEventPort,
            CodeGradingService codeGradingService,
            SubmissionAttemptPolicy submissionAttemptPolicy,
            SubmissionCodePolicy submissionCodePolicy
    ) {
        this.submissionCommandPort = submissionCommandPort;
        this.loadProblemForSubmissionPort = loadProblemForSubmissionPort;
        this.loadTestCasesForGradingPort = loadTestCasesForGradingPort;
        this.problemProgressPort = problemProgressPort;
        this.problemSetCompletionEventPort = problemSetCompletionEventPort;
        this.codeGradingService = codeGradingService;
        this.submissionAttemptPolicy = submissionAttemptPolicy;
        this.submissionCodePolicy = submissionCodePolicy;
    }

    @Override
    @Transactional
    public SubmissionView handle(Long problemId, SubmitCodeCommand command) {
        submissionCodePolicy.validate(command.code());

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

        var testCases = loadTestCasesForGradingPort.loadActiveTestCases(problemId);
        var gradingResult = codeGradingService.grade(command.code(), testCases);

        boolean isCorrect = gradingResult.correct();
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

        Long submissionId = submissionCommandPort.saveCodeSubmission(new CodeSubmission(
                command.userId(),
                problem.problemId(),
                command.code(),
                isCorrect,
                attemptNo,
                gradingResult.passedTestCount(),
                gradingResult.totalTestCount(),
                gradingResult.executionStatus(),
                gradingResult.errorMessage()
        ));
        submissionCommandPort.saveTestResults(submissionId, gradingResult.testResults());

        Long nextProblemId = null;
        boolean isProblemSetCompleted = false;

        if (isCorrect) {
            nextProblemId = loadProblemForSubmissionPort
                    .findNextProblemId(problemSetId, problem.problemOrder() + 1)
                    .orElse(null);

            if (nextProblemId == null) {
                isProblemSetCompleted = true;
                problemProgressPort.completeProblemSet(command.userId(), problemSetId);
                problemSetCompletionEventPort.publishCompleted(command.userId(), problemSetId);
            } else {
                problemProgressPort.openNextProblem(command.userId(), problemSetId);
            }
        }

        return new SubmissionView(
                submissionId,
                problem.problemId(),
                isCorrect,
                gradingResult.passedTestCount(),
                gradingResult.totalTestCount(),
                gradingResult.executionStatus(),
                gradingResult.errorMessage(),
                attemptNo,
                remainingAttemptCount,
                canRetry,
                nextProblemId,
                isProblemSetCompleted,
                isCorrect ? problem.explanation() : null
        );
    }
}