package com.wanted.codebombalms.learning.infrastructure.problem;

import com.wanted.codebombalms.learning.application.port.LearningProblemPort;
import com.wanted.codebombalms.problems.problem.application.service.ProblemQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningProblemAdapter implements LearningProblemPort {

    private final ProblemQueryService problemQueryService;

    @Override
    public ProblemForLearning loadProblem(Long problemId) {
        var problem = problemQueryService.findProblemForSubmission(problemId);
        return new ProblemForLearning(
                problem.problemId(),
                problem.explanation(),
                problem.attemptLimit(),
                problem.retriable()
        );
    }
}
