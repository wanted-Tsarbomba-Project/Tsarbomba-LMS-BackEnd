package com.wanted.codebombalms.learning.application.port;

import java.util.List;

public interface LearningProblemPort {

    ProblemSetForLearning loadProblemSet(Long problemSetId);

    ProblemForLearning loadProblem(Long problemId);

    boolean existsProblem(Long problemId);

    boolean existsProblemInSet(Long problemSetId, Long problemId);

    record ProblemForLearning(
            Long problemId,
            Long problemSetId,
            Integer problemNumber,
            String explanation,
            Integer point,
            Integer attemptLimit,
            Boolean retriable
    ) {
    }

    record ProblemSetForLearning(
            Long problemSetId,
            String title,
            String description,
            List<ProblemDetailForLearning> problems
    ) {
    }

    record ProblemDetailForLearning(
            Long problemId,
            Integer problemNumber,
            String title,
            String content,
            String problemType,
            Integer point,
            String startCode
    ) {
    }
}
