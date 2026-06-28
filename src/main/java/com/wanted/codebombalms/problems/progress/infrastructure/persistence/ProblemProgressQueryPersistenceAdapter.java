package com.wanted.codebombalms.problems.progress.infrastructure.persistence;
import com.wanted.codebombalms.problems.progress.application.port.LoadProblemsForProgressPort;
import com.wanted.codebombalms.problems.progress.application.port.LoadProgressProblemPort;
import com.wanted.codebombalms.problems.progress.domain.model.ProblemProgressItem;
import com.wanted.codebombalms.submission.application.port.SubmissionQueryPort;
import com.wanted.codebombalms.submission.domain.model.LatestSubmission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        var problems = loadProblemsForProgressPort.loadActiveProblems(problemSetId);

        var problemIds = problems.stream()
                .map(LoadProblemsForProgressPort.ProgressProblem::problemId)
                .toList();

        Map<Long, LatestSubmission> latestSubmissionByProblemId =
                submissionQueryPort.findLatestResults(userId, problemIds)
                        .stream()
                        .collect(Collectors.toMap(
                                LatestSubmission::problemId,
                                Function.identity(),
                                this::selectLatestSubmission

                        ));

        return problems.stream()
                .map(problem -> toProgressItem(
                        currentProblemNumber,
                        problem,
                        latestSubmissionByProblemId.get(problem.problemId())
                ))
                .toList();
    }

    private LatestSubmission selectLatestSubmission(
            LatestSubmission first,
            LatestSubmission second
    ) {
        int submittedAtComparison =
                first.submittedAt().compareTo(second.submittedAt());

        if (submittedAtComparison != 0) {
            return submittedAtComparison > 0 ? first : second;
        }

        return first.submissionId() >= second.submissionId()
                ? first
                : second;
    }

    private ProblemProgressItem toProgressItem(
            Integer currentProblemNumber,
            LoadProblemsForProgressPort.ProgressProblem problem,
            LatestSubmission latestSubmission
    ) {
        Boolean latestCorrect = latestSubmission == null
                ? null
                : latestSubmission.correct();

        Long latestSubmissionId = latestSubmission == null
                ? null
                : latestSubmission.submissionId();

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
