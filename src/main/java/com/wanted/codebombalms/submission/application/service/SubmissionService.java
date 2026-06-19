package com.wanted.codebombalms.submission.application.service;

import com.wanted.codebombalms.problems.dataset.application.port.GenerateDatasetAccessUrlPort;
import com.wanted.codebombalms.problems.dataset.application.port.LoadActiveDatasetFilePathPort;
import com.wanted.codebombalms.submission.application.command.SubmitCodeCommand;
import com.wanted.codebombalms.submission.application.policy.SubmissionAttemptPolicy;
import com.wanted.codebombalms.submission.application.policy.SubmissionCodePolicy;
import com.wanted.codebombalms.submission.application.port.LoadProblemForSubmissionPort;
import com.wanted.codebombalms.submission.application.port.LoadProblemForSubmissionPort.ProblemForSubmission;
import com.wanted.codebombalms.submission.application.port.LoadTestCasesForGradingPort;
import com.wanted.codebombalms.submission.application.port.ProblemProgressPort;
import com.wanted.codebombalms.submission.application.port.ProblemSetCompletionEventPort;
import com.wanted.codebombalms.submission.application.port.ProblemSolvedEventPort;
import com.wanted.codebombalms.submission.application.port.SubmissionCommandPort;
import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase;
import com.wanted.codebombalms.submission.domain.model.CodeSubmission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService implements SubmissionCommandUseCase {

    private final SubmissionCommandPort submissionCommandPort;
    private final LoadProblemForSubmissionPort loadProblemForSubmissionPort;
    private final LoadTestCasesForGradingPort loadTestCasesForGradingPort;
    private final ProblemProgressPort problemProgressPort;
    private final ProblemSetCompletionEventPort problemSetCompletionEventPort;
    private final CodeGradingService codeGradingService;
    private final SubmissionAttemptPolicy submissionAttemptPolicy;
    private final SubmissionCodePolicy submissionCodePolicy;
    private final ProblemSolvedEventPort problemSolvedEventPort;
    private final LoadActiveDatasetFilePathPort loadActiveDatasetFilePathPort;
    private final GenerateDatasetAccessUrlPort generateDatasetAccessUrlPort;

    @Override
    @Transactional
    public SubmissionView handle(Long problemId, SubmitCodeCommand command) {
        long startNanos = System.nanoTime();

        try {
        submissionCodePolicy.validate(command.code());

        ProblemForSubmission problem =
                loadProblemForSubmissionPort.loadProblemForSubmission(problemId);

        Long problemSetId = problem.problemSetId();

        validateNotAlreadySolved(command.userId(), problemId);

        problemProgressPort.validateCurrentProblem(
                command.userId(),
                problemSetId,
                problem.problemOrder()
        );

        int previousAttemptCount =
                submissionCommandPort.countAttempts(
                        command.userId(),
                        problemId
                );

        submissionAttemptPolicy.validateAttemptLimit(
                problem.attemptLimit(),
                problem.retriable(),
                previousAttemptCount
        );

        String datasetAccessUrl = generateDatasetAccessUrl(problemSetId);

        var testCases = loadTestCasesForGradingPort.loadActiveTestCases(problemId);

        var gradingResult = codeGradingService.grade(
                command.code(),
                datasetAccessUrl,
                testCases
        );

        boolean isCorrect = gradingResult.correct();
        int attemptNo = previousAttemptCount + 1;
        Integer remainingAttemptCount = null;

        boolean canRetry = submissionAttemptPolicy.canRetry(
                problem.retriable(),
                remainingAttemptCount,
                isCorrect
        );

        Long submissionId = submissionCommandPort.saveCodeSubmission(
                new CodeSubmission(
                        command.userId(),
                        problem.problemId(),
                        command.code(),
                        isCorrect,
                        attemptNo,
                        gradingResult.passedTestCount(),
                        gradingResult.totalTestCount(),
                        gradingResult.executionStatus(),
                        gradingResult.errorMessage()
                )
        );

        submissionCommandPort.saveTestResults(
                submissionId,
                gradingResult.testResults()
        );

        Long nextProblemId = null;
        boolean isProblemSetCompleted = false;
        int earnedPoint = 0;
        boolean pointGranted = false;

        if (isCorrect) {
            problemSolvedEventPort.publishSolved(
                    command.userId(),
                    problem.problemId(),
                    submissionId,
                    problem.point()
            );

            earnedPoint = problem.point();
            pointGranted = true;

            nextProblemId = loadProblemForSubmissionPort
                    .findNextProblemId(
                            problemSetId,
                            problem.problemOrder() + 1
                    )
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

        SubmissionView view = new SubmissionView(
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
                earnedPoint,
                pointGranted,
                isCorrect ? problem.explanation() : null
        );

        log.info(
                "event=submission_completed userId={} problemId={} submissionId={} isCorrect={} passedTestCount={} totalTestCount={} durationMs={}",
                command.userId(),
                problem.problemId(),
                submissionId,
                isCorrect,
                gradingResult.passedTestCount(),
                gradingResult.totalTestCount(),
                elapsedMillis(startNanos)
        );

        return view;
        } catch (RuntimeException e) {
            log.warn(
                    "event=submission_failed userId={} problemId={} exceptionType={} durationMs={}",
                    command.userId(),
                    problemId,
                    e.getClass().getSimpleName(),
                    elapsedMillis(startNanos),
                    e
            );
            throw e;
        }
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private void validateNotAlreadySolved(Long userId, Long problemId) {
        boolean alreadySolved =
                submissionCommandPort.existsCorrectSubmission(
                        userId,
                        problemId
                );

        submissionAttemptPolicy.validateNotAlreadySolved(alreadySolved);
    }
    private String generateDatasetAccessUrl(Long problemSetId) {
        String filePath =
                loadActiveDatasetFilePathPort.loadActiveDatasetFilePath(problemSetId);

        if (filePath == null || filePath.isBlank()) {
            return null;
        }

        return generateDatasetAccessUrlPort.generate(filePath);
    }
}
