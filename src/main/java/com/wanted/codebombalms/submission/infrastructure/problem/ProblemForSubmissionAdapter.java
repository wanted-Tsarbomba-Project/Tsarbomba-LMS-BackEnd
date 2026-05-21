package com.wanted.codebombalms.submission.infrastructure.problem;

import com.wanted.codebombalms.problems.problem.application.service.ProblemQueryService;
import com.wanted.codebombalms.submission.application.port.LoadProblemForSubmissionPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProblemForSubmissionAdapter implements LoadProblemForSubmissionPort {

    private final ProblemQueryService problemQueryService;

    public ProblemForSubmissionAdapter(ProblemQueryService problemQueryService) {
        this.problemQueryService = problemQueryService;
    }

    @Override
    public ProblemForSubmission loadProblem(Long problemId) {
        var problem = problemQueryService.findProblemForSubmission(problemId);

        return new ProblemForSubmission(
                problem.problemId(),
                problem.problemSetId(),
                problem.problemOrder(),
                problem.answer(),
                problem.explanation(),
                problem.score(),
                problem.attemptLimit(),
                problem.retriable()
        );
    }

    @Override
    public Optional<Long> findNextProblemId(Long problemSetId, Integer nextProblemOrder) {
        return problemQueryService.findProblemIdByProblemSetAndOrder(problemSetId, nextProblemOrder);
    }
}
