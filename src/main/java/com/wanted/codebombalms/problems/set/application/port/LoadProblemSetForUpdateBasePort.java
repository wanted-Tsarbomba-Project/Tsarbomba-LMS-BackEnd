package com.wanted.codebombalms.problems.set.application.port;

public interface LoadProblemSetForUpdateBasePort {

    ProblemSetForUpdateBase loadBase(Long problemSetId);

    record ProblemSetForUpdateBase(
            Long problemSetId,
            String title,
            String categoryName,
            String difficulty,
            String description
    ) {
    }
}
