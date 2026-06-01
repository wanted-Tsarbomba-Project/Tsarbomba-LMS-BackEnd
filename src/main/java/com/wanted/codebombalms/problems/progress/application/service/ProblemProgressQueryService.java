package com.wanted.codebombalms.problems.progress.application.service;

import com.wanted.codebombalms.problems.progress.application.query.GetProblemProgressQuery;
import com.wanted.codebombalms.problems.progress.application.usecase.GetProblemProgressUseCase;
import com.wanted.codebombalms.problems.progress.domain.model.ProblemProgress;
import com.wanted.codebombalms.problems.progress.domain.model.ProblemProgressItem;
import com.wanted.codebombalms.problems.progress.application.port.CheckProgressProblemSetPort;
import com.wanted.codebombalms.problems.progress.application.port.LoadCurrentProgressPort;
import com.wanted.codebombalms.problems.progress.application.port.LoadProgressProblemPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProblemProgressQueryService implements GetProblemProgressUseCase {

    private final CheckProgressProblemSetPort checkProgressProblemSetPort;
    private final LoadCurrentProgressPort loadCurrentProgressPort;
    private final LoadProgressProblemPort loadProgressProblemPort;

    @Override
    @Transactional(readOnly = true)
    public ProblemProgressView handle(GetProblemProgressQuery query) {
        checkProgressProblemSetPort.checkProblemSetExists(query.problemSetId());

        Integer currentProblemNumber = loadCurrentProgressPort.loadCurrentProblemNumber(
                query.userId(),
                query.problemSetId()
        );

        ProblemProgress progress = ProblemProgress.of(
                query.problemSetId(),
                currentProblemNumber,
                loadProgressProblemPort.loadProgressProblems(
                        query.userId(),
                        query.problemSetId(),
                        currentProblemNumber
                )
        );

        return toView(progress);
    }

    private ProblemProgressView toView(ProblemProgress progress) {
        return new ProblemProgressView(
                progress.getProblemSetId(),
                progress.getTotalProblemCount(),
                progress.getCurrentProblemNumber(),
                progress.getCurrentProblemId(),
                progress.getSolvedProblemCount(),
                progress.getProblems()
                        .stream()
                        .map(this::toView)
                        .toList()
        );
    }

    private ProblemProgressItemView toView(ProblemProgressItem problem) {
        return new ProblemProgressItemView(
                problem.getProblemId(),
                problem.getProblemNumber(),
                problem.getTitle(),
                problem.getStatus().name(),
                problem.getLatestSubmissionId()
        );
    }
}

