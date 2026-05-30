package com.wanted.codebombalms.problems.set.application.service;

import com.wanted.codebombalms.problems.progress.application.port.LoadProgressProblemPort;
import com.wanted.codebombalms.problems.progress.domain.model.ProblemProgressItem;
import com.wanted.codebombalms.problems.set.application.port.FindOrCreateProblemSetProgressPort;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemForEntryPort;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemSetEntryPort;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemStartCodePort;
import com.wanted.codebombalms.problems.set.application.query.EnterProblemSetQuery;
import com.wanted.codebombalms.problems.set.application.usecase.EnterProblemSetUseCase;
import com.wanted.codebombalms.problems.set.domain.model.ProblemDetail;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetEntry;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetProgressState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemSetEntryService implements EnterProblemSetUseCase {

    private final LoadProblemSetEntryPort loadProblemSetEntryPort;
    private final FindOrCreateProblemSetProgressPort findOrCreateProblemSetProgressPort;
    private final LoadProblemForEntryPort loadProblemForEntryPort;
    private final LoadProblemStartCodePort loadProblemStartCodePort;
    private final LoadProgressProblemPort loadProgressProblemPort;

    @Override
    @Transactional
    public ProblemSetEntryView handle(EnterProblemSetQuery query) {
        ProblemSetEntry problemSet = loadProblemSetEntryPort.loadProblemSetEntry(query.problemSetId());

        ProblemSetProgressState progress =
                findOrCreateProblemSetProgressPort.findOrCreateProgress(query.userId(), query.problemSetId());

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

        return new ProblemSetEntryView(
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
