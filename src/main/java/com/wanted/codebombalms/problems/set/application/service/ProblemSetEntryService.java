package com.wanted.codebombalms.problems.set.application.service;

import com.wanted.codebombalms.problems.progress.application.port.LoadProgressProblemPort;
import com.wanted.codebombalms.problems.progress.domain.model.ProblemProgressItem;
import com.wanted.codebombalms.problems.set.application.port.*;
import com.wanted.codebombalms.problems.set.application.query.EnterProblemSetQuery;
import com.wanted.codebombalms.problems.set.application.usecase.EnterProblemSetUseCase;
import com.wanted.codebombalms.problems.set.domain.model.ProblemDetail;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetEntry;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetProgressState;
import com.wanted.codebombalms.problems.set.application.usecase.ValidateProblemSetAccessUseCase;
import com.wanted.codebombalms.problems.set.infrastructure.metrics.ProblemSetEntryMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemSetEntryService implements EnterProblemSetUseCase {

    private final LoadProblemSetEntryPort loadProblemSetEntryPort;
    private final FindOrCreateProblemSetProgressPort findOrCreateProblemSetProgressPort;
    private final LoadProblemForEntryPort loadProblemForEntryPort;
    private final LoadProblemStartCodePort loadProblemStartCodePort;
    private final LoadProgressProblemPort loadProgressProblemPort;
    private final IncreaseProblemSetStartedCountPort increaseProblemSetStartedCountPort;
    private final ValidateProblemSetAccessUseCase validateProblemSetAccessUseCase;
    private final ProblemSetEntryMetrics problemSetEntryMetrics;

    @Override
    @Transactional
    public ProblemSetEntryView handle(EnterProblemSetQuery query) {
        long startNanos = System.nanoTime();

        try {
        long accessStartedAt = System.nanoTime();
        validateProblemSetAccessUseCase.validate(
                query.userId(),
                query.problemSetId()
        );
        long accessNanos = elapsedNanos(accessStartedAt);
        problemSetEntryMetrics.recordAccess(accessNanos);

        long problemSetStartedAt = System.nanoTime();
        ProblemSetEntry problemSet =
                loadProblemSetEntryPort.loadProblemSetEntry(query.problemSetId());
        long problemSetNanos = elapsedNanos(problemSetStartedAt);
        problemSetEntryMetrics.recordProblemSet(problemSetNanos);

        long progressStartedAt = System.nanoTime();
        ProblemSetProgressState progress =
                findOrCreateProblemSetProgressPort.findOrCreateProgress(query.userId(), query.problemSetId());
        if (Boolean.TRUE.equals(progress.created())) {
            increaseProblemSetStartedCountPort.increaseStartedUserCount(query.problemSetId());
        }
        long progressNanos = elapsedNanos(progressStartedAt);
        problemSetEntryMetrics.recordProgress(progressNanos);

        long progressItemsStartedAt = System.nanoTime();
        var progressItems = loadProgressProblemPort.loadProgressProblems(
                query.userId(),
                query.problemSetId(),
                progress.currentProblemNumber()
        );
        long progressItemsNanos = elapsedNanos(progressItemsStartedAt);
        problemSetEntryMetrics.recordProgressItems(progressItemsNanos);

        Map<Long, ProblemProgressItem> progressItemMap = progressItems.stream()
                .collect(Collectors.toMap(
                        ProblemProgressItem::getProblemId,
                        Function.identity()
                ));

        long problemDetailsStartedAt = System.nanoTime();
        var problems = loadProblemForEntryPort.loadProblems(query.problemSetId())
                .stream()
                .map(problem -> toDetailItemView(problem, progressItemMap.get(problem.getProblemId())))
                .toList();
        long problemDetailsNanos = elapsedNanos(problemDetailsStartedAt);
        problemSetEntryMetrics.recordProblemDetails(problemDetailsNanos);

        Long currentProblemId = problems.stream()
                .filter(problem -> problem.problemNumber().equals(progress.currentProblemNumber()))
                .map(ProblemDetailItemView::problemId)
                .findFirst()
                .orElse(null);

        int solvedProblemCount = (int) problems.stream()
                .filter(problem -> "CORRECT".equals(problem.status()))
                .count();

        ProblemSetEntryView view = new ProblemSetEntryView(
                problemSet.getProblemSetId(),
                problemSet.getTitle(),
                problemSet.getDescription(),
                progress.currentProblemNumber(),
                currentProblemId,
                problems.size(),
                solvedProblemCount,
                progress.completed(),
                problems
        );

        long totalNanos = elapsedNanos(startNanos);

        log.info(
                "event=problem_set_entered userId={} problemSetId={} problemCount={} solvedProblemCount={} "
                        + "accessMs={} problemSetMs={} progressMs={} progressItemsMs={} problemDetailsMs={} durationMs={}",
                query.userId(),
                query.problemSetId(),
                problems.size(),
                solvedProblemCount,
                nanosToMillis(accessNanos),
                nanosToMillis(problemSetNanos),
                nanosToMillis(progressNanos),
                nanosToMillis(progressItemsNanos),
                nanosToMillis(problemDetailsNanos),
                nanosToMillis(totalNanos)
        );

        return view;
        } catch (RuntimeException e) {
            log.warn(
                    "event=problem_set_entry_failed userId={} problemSetId={} exceptionType={} durationMs={}",
                    query.userId(),
                    query.problemSetId(),
                    e.getClass().getSimpleName(),
                    nanosToMillis(elapsedNanos(startNanos)),
                    e
            );
            throw e;
        } finally {
            problemSetEntryMetrics.recordTotal(elapsedNanos(startNanos));
        }
    }

    private long elapsedNanos(long startNanos) {
        return System.nanoTime() - startNanos;
    }

    private long nanosToMillis(long elapsedNanos) {
        return elapsedNanos / 1_000_000;
    }

    private ProblemDetailItemView toDetailItemView(
            ProblemDetail problem,
            ProblemProgressItem progressItem
    ) {
        String startCode = loadProblemStartCodePort.loadStartCode(problem.getProblemId());

        return new ProblemDetailItemView(
                problem.getProblemId(),
                problem.getProblemNumber(),
                problem.getTitle(),
                problem.getContent(),
                problem.getProblemType(),
                problem.getPoint(),
                startCode,
                progressItem == null ? "UNSOLVED" : progressItem.getStatus().name(),
                progressItem == null ? null : progressItem.getLatestSubmissionId()
        );
    }
}
