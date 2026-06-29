package com.wanted.codebombalms.submission.application.service;

import com.wanted.codebombalms.problems.dataset.application.port.LoadActiveDatasetFilePathPort;
import com.wanted.codebombalms.submission.application.command.SubmitCodeCommand;
import com.wanted.codebombalms.submission.application.policy.SubmissionAttemptPolicy;
import com.wanted.codebombalms.submission.application.port.LoadProblemForSubmissionPort;
import com.wanted.codebombalms.submission.application.port.LoadProblemForSubmissionPort.ProblemForSubmission;
import com.wanted.codebombalms.submission.application.port.LoadTestCasesForGradingPort;
import com.wanted.codebombalms.submission.application.port.LoadTestCasesForGradingPort.TestCaseForGrading;
import com.wanted.codebombalms.submission.application.port.ProblemProgressPort;
import com.wanted.codebombalms.submission.application.port.ProblemSetCompletionEventPort;
import com.wanted.codebombalms.submission.application.port.ProblemSolvedEventPort;
import com.wanted.codebombalms.submission.application.port.SubmissionCommandPort;
import com.wanted.codebombalms.submission.application.service.CodeGradingService.CodeGradingResult;
import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase.SubmissionView;
import com.wanted.codebombalms.submission.domain.model.CodeSubmission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionTransactionService {

    private final SubmissionCommandPort submissionCommandPort;
    private final LoadProblemForSubmissionPort loadProblemForSubmissionPort;
    private final LoadTestCasesForGradingPort loadTestCasesForGradingPort;
    private final ProblemProgressPort problemProgressPort;
    private final ProblemSetCompletionEventPort problemSetCompletionEventPort;
    private final ProblemSolvedEventPort problemSolvedEventPort;
    private final LoadActiveDatasetFilePathPort loadActiveDatasetFilePathPort;
    private final SubmissionAttemptPolicy submissionAttemptPolicy;

    @Transactional(readOnly = true)
    public SubmissionPreparation prepare(
            Long problemId,
            SubmitCodeCommand command
    ) {
        ProblemForSubmission problem =
                loadProblemForSubmissionPort.loadProblemForSubmission(problemId);

        validateSubmissionState(problem, command);

        String datasetFilePath =
                loadActiveDatasetFilePathPort.loadActiveDatasetFilePath(
                        problem.problemSetId()
                );

        List<TestCaseForGrading> testCases =
                loadTestCasesForGradingPort.loadActiveTestCases(problemId);

        return new SubmissionPreparation(
                datasetFilePath,
                List.copyOf(testCases)
        );
    }

    @Transactional
    public SubmissionView complete(
            Long problemId,
            SubmitCodeCommand command,
            CodeGradingResult gradingResult
    ) {
        // 채점 중 상태가 바뀔 수 있으므로 저장 직전에 다시 검증한다.
        ProblemForSubmission problem =
                loadProblemForSubmissionPort.loadProblemForSubmission(problemId);

        int previousAttemptCount = validateSubmissionState(problem, command);
        int attemptNo = previousAttemptCount + 1;
        boolean isCorrect = gradingResult.correct();

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
        boolean problemSetCompleted = false;
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
                            problem.problemSetId(),
                            problem.problemOrder() + 1
                    )
                    .orElse(null);

            if (nextProblemId == null) {
                problemSetCompleted = true;

                problemProgressPort.completeProblemSet(
                        command.userId(),
                        problem.problemSetId()
                );

                problemSetCompletionEventPort.publishCompleted(
                        command.userId(),
                        problem.problemSetId()
                );
            } else {
                problemProgressPort.openNextProblem(
                        command.userId(),
                        problem.problemSetId()
                );
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
                problemSetCompleted,
                earnedPoint,
                pointGranted,
                isCorrect ? problem.explanation() : null
        );
    }

    private int validateSubmissionState(
            ProblemForSubmission problem,
            SubmitCodeCommand command
    ) {
        boolean alreadySolved =
                submissionCommandPort.existsCorrectSubmission(
                        command.userId(),
                        problem.problemId()
                );

        submissionAttemptPolicy.validateNotAlreadySolved(alreadySolved);

        problemProgressPort.validateCurrentProblem(
                command.userId(),
                problem.problemSetId(),
                problem.problemOrder()
        );

        int previousAttemptCount =
                submissionCommandPort.countAttempts(
                        command.userId(),
                        problem.problemId()
                );

        submissionAttemptPolicy.validateAttemptLimit(
                problem.attemptLimit(),
                problem.retriable(),
                previousAttemptCount
        );

        return previousAttemptCount;
    }

    public record SubmissionPreparation(
            String datasetFilePath,
            List<TestCaseForGrading> testCases
    ) {
    }
}
