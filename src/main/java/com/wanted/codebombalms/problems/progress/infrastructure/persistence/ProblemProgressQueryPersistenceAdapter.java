package com.wanted.codebombalms.problems.progress.infrastructure.persistence;
import com.wanted.codebombalms.problems.progress.application.port.LoadProblemsForProgressPort;
import com.wanted.codebombalms.problems.progress.application.port.LoadProgressProblemPort;
import com.wanted.codebombalms.problems.progress.domain.model.ProblemProgressItem;
import com.wanted.codebombalms.submission.application.port.SubmissionQueryPort;
import com.wanted.codebombalms.submission.domain.model.LatestSubmission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProblemProgressQueryPersistenceAdapter implements LoadProgressProblemPort {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final LoadProblemsForProgressPort loadProblemsForProgressPort;
    private final SubmissionQueryPort submissionQueryPort;

    @Override
    public List<ProblemProgressItem> loadProgressProblems(
            Long userId,
            Long problemSetId,
            Integer currentProblemNumber
    ) {
        return loadProblemsForProgressPort.loadActiveProblems(problemSetId)
                .stream()
                .map(problem -> toProgressItem(userId, currentProblemNumber, problem))
                .toList();
    }

    private ProblemProgressItem toProgressItem(
            Long userId,
            Integer currentProblemNumber,
            LoadProblemsForProgressPort.ProgressProblem problem
    ) {
        var latestSubmission = submissionQueryPort
                .findLatestResult(userId, problem.problemId());

        Boolean latestCorrect = latestSubmission
                .map(LatestSubmission::correct)
                .orElse(null);

        Long latestSubmissionId = latestSubmission
                .map(LatestSubmission::submissionId)
                .orElse(null);

        return ProblemProgressItem.of(
                problem.problemId(),
                problem.problemOrder(),
                problem.title(),
                currentProblemNumber,
                latestCorrect,
                latestSubmissionId
        );
    }
}
