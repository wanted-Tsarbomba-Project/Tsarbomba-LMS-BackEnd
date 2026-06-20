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

    @Override
    @Transactional
    public ProblemSetEntryView handle(EnterProblemSetQuery query) {
        long startNanos = System.nanoTime();

        try {
        validateProblemSetAccessUseCase.validate(
                query.userId(),
                query.problemSetId()
        );
        ProblemSetEntry problemSet =
                loadProblemSetEntryPort.loadProblemSetEntry(query.problemSetId());

        ProblemSetProgressState progress =
                findOrCreateProblemSetProgressPort.findOrCreateProgress(query.userId(), query.problemSetId());
        if (Boolean.TRUE.equals(progress.created())) {
            increaseProblemSetStartedCountPort.increaseStartedUserCount(query.problemSetId());
        }

        var progressItems = loadProgressProblemPort.loadProgressProblems(
                query.userId(),
                query.problemSetId(),
                progress.currentProblemNumber()
        );

        Map<Long, ProblemProgressItem> progressItemMap = progressItems.stream()
                .collect(Collectors.toMap(
                        ProblemProgressItem::getProblemId,
                        Function.identity()
                ));

        var problems = loadProblemForEntryPort.loadProblems(query.problemSetId())
                .stream()
                .map(problem -> toDetailItemView(problem, progressItemMap.get(problem.getProblemId())))
                .toList();

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

        log.info(
                "event=problem_set_entered userId={} problemSetId={} problemCount={} solvedProblemCount={} durationMs={}",
                query.userId(),
                query.problemSetId(),
                problems.size(),
                solvedProblemCount,
                elapsedMillis(startNanos)
        );

        return view;
        } catch (RuntimeException e) {
            log.warn(
                    "event=problem_set_entry_failed userId={} problemSetId={} exceptionType={} durationMs={}",
                    query.userId(),
                    query.problemSetId(),
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
