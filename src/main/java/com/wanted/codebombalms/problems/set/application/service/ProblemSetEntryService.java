package com.wanted.codebombalms.problems.set.application.service;

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

@Service
@RequiredArgsConstructor
public class ProblemSetEntryService implements EnterProblemSetUseCase {

    private final LoadProblemSetEntryPort loadProblemSetEntryPort;
    private final FindOrCreateProblemSetProgressPort findOrCreateProblemSetProgressPort;
    private final LoadProblemForEntryPort loadProblemForEntryPort;
    private final LoadProblemStartCodePort loadProblemStartCodePort;

    @Override
    @Transactional
    public ProblemSetEntryView handle(EnterProblemSetQuery query) {
        ProblemSetEntry problemSet = loadProblemSetEntryPort.loadProblemSetEntry(query.problemSetId());
        ProblemSetProgressState progress =
                findOrCreateProblemSetProgressPort.findOrCreateProgress(query.userId(), query.problemSetId());

        ProblemDetail currentProblem = Boolean.TRUE.equals(progress.completed())
                ? loadProblemForEntryPort.loadLastProblem(query.problemSetId()).orElse(null)
                : loadProblemForEntryPort.loadCurrentProblem(
                        query.problemSetId(),
                        progress.currentProblemNumber()
                );

        if (currentProblem != null) {
            currentProblem = currentProblem.withStartCode(
                    loadProblemStartCodePort.loadStartCode(currentProblem.getProblemId())
            );
        }

        return new ProblemSetEntryView(
                problemSet.getProblemSetId(),
                problemSet.getTitle(),
                problemSet.getDescription(),
                progress.currentProblemNumber(),
                progress.completed(),
                toView(currentProblem)
        );
    }

    private ProblemDetailView toView(ProblemDetail problem) {
        if (problem == null) {
            return null;
        }

        return new ProblemDetailView(
                problem.getProblemId(),
                problem.getProblemNumber(),
                problem.getTitle(),
                problem.getContent(),
                problem.getProblemType(),
                problem.getStartCode()
        );
    }
}
