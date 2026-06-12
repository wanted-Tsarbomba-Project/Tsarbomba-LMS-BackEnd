package com.wanted.codebombalms.learning.infrastructure.problem;

import com.wanted.codebombalms.course.application.port.ProblemCatalogPort;
import com.wanted.codebombalms.learning.application.port.LearningProblemPort;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemForEntryPort;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemStartCodePort;
import com.wanted.codebombalms.problems.set.application.port.LoadProblemSetEntryPort;
import com.wanted.codebombalms.submission.application.port.LoadProblemForSubmissionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningProblemAdapter implements LearningProblemPort {

    private final LoadProblemForSubmissionPort loadProblemForSubmissionPort;
    private final LoadProblemSetEntryPort loadProblemSetEntryPort;
    private final LoadProblemForEntryPort loadProblemForEntryPort;
    private final LoadProblemStartCodePort loadProblemStartCodePort;
    private final ProblemCatalogPort problemCatalogPort;

    @Override
    public ProblemSetForLearning loadProblemSet(Long problemSetId) {
        var problemSet = loadProblemSetEntryPort.loadProblemSetEntry(problemSetId);
        var loadedProblems = loadProblemForEntryPort.loadProblems(problemSetId);
        String startCode = loadedProblems.isEmpty()
                ? null
                : loadProblemStartCodePort.loadStartCode(loadedProblems.get(0).getProblemId());
        var problems = loadedProblems
                .stream()
                .map(problem -> new ProblemDetailForLearning(
                        problem.getProblemId(),
                        problem.getProblemNumber(),
                        problem.getTitle(),
                        problem.getContent(),
                        problem.getProblemType(),
                        problem.getPoint(),
                        startCode
                ))
                .toList();

        return new ProblemSetForLearning(
                problemSet.getProblemSetId(),
                problemSet.getTitle(),
                problemSet.getDescription(),
                problems
        );
    }

    @Override
    public ProblemForLearning loadProblem(Long problemId) {
        var problem = loadProblemForSubmissionPort.loadProblemForSubmission(problemId);
        return new ProblemForLearning(
                problem.problemId(),
                problem.problemSetId(),
                problem.problemOrder(),
                problem.explanation(),
                problem.point(),
                problem.attemptLimit(),
                problem.retriable()
        );
    }

    @Override
    public boolean existsProblem(Long problemId) {
        return problemCatalogPort.existsProblem(problemId);
    }

    @Override
    public boolean existsProblemInSet(Long problemSetId, Long problemId) {
        return problemCatalogPort.existsProblemInSet(problemSetId, problemId);
    }
}
