package com.wanted.codebombalms.submission.application.service;

import com.wanted.codebombalms.problems.dataset.application.port.GenerateDatasetAccessUrlPort;
import com.wanted.codebombalms.submission.application.command.SubmitCodeCommand;
import com.wanted.codebombalms.submission.application.policy.SubmissionCodePolicy;
import com.wanted.codebombalms.submission.application.service.CodeGradingService.CodeGradingResult;
import com.wanted.codebombalms.submission.application.service.SubmissionTransactionService.SubmissionPreparation;
import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase;
import com.wanted.codebombalms.submission.infrastructure.metrics.SubmissionMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService implements SubmissionCommandUseCase {

    private final SubmissionTransactionService submissionTransactionService;
    private final CodeGradingService codeGradingService;
    private final SubmissionCodePolicy submissionCodePolicy;
    private final GenerateDatasetAccessUrlPort generateDatasetAccessUrlPort;
    private final SubmissionMetrics submissionMetrics;

    @Override
    public SubmissionView handle(
            Long problemId,
            SubmitCodeCommand command
    ) {
        long totalStartNanos = System.nanoTime();

        long prepareNanos = 0;
        long gradingNanos = 0;
        long saveNanos = 0;

        try {
            submissionCodePolicy.validate(command.code());

            long prepareStartNanos = System.nanoTime();
            SubmissionPreparation preparation;

            try {
                preparation = submissionTransactionService.prepare(
                        problemId,
                        command
                );
            } finally {
                prepareNanos = elapsedNanos(prepareStartNanos);
                submissionMetrics.recordPrepare(prepareNanos);
            }

            String datasetAccessUrl =
                    generateDatasetAccessUrl(preparation.datasetFilePath());

            long gradingStartNanos = System.nanoTime();
            CodeGradingResult gradingResult;

            try {
                gradingResult = codeGradingService.grade(
                        command.code(),
                        datasetAccessUrl,
                        preparation.testCases()
                );
            } finally {
                gradingNanos = elapsedNanos(gradingStartNanos);
                submissionMetrics.recordGrading(gradingNanos);
            }

            long saveStartNanos = System.nanoTime();
            SubmissionView view;

            try {
                view = submissionTransactionService.complete(
                        problemId,
                        command,
                        gradingResult
                );
            } finally {
                saveNanos = elapsedNanos(saveStartNanos);
                submissionMetrics.recordSave(saveNanos);
            }

            log.info(
                    "event=submission_completed userId={} problemId={} submissionId={} "
                            + "isCorrect={} passedTestCount={} totalTestCount={} "
                            + "prepareMs={} gradingMs={} saveMs={} durationMs={}",
                    command.userId(),
                    view.problemId(),
                    view.submissionId(),
                    view.correct(),
                    view.passedTestCount(),
                    view.totalTestCount(),
                    nanosToMillis(prepareNanos),
                    nanosToMillis(gradingNanos),
                    nanosToMillis(saveNanos),
                    elapsedMillis(totalStartNanos)
            );

            return view;
        } catch (RuntimeException e) {
            log.warn(
                    "event=submission_failed userId={} problemId={} "
                            + "exceptionType={} durationMs={}",
                    command.userId(),
                    problemId,
                    e.getClass().getSimpleName(),
                    elapsedMillis(totalStartNanos),
                    e
            );

            throw e;
        } finally {
            submissionMetrics.recordTotal(
                    elapsedNanos(totalStartNanos)
            );
        }
    }

    private String generateDatasetAccessUrl(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return null;
        }

        return generateDatasetAccessUrlPort.generate(filePath);
    }

    private long elapsedNanos(long startNanos) {
        return System.nanoTime() - startNanos;
    }

    private long nanosToMillis(long nanos) {
        return nanos / 1_000_000;
    }

    private long elapsedMillis(long startNanos) {
        return nanosToMillis(elapsedNanos(startNanos));
    }
}
