package com.wanted.codebombalms.problems.problem.application.port;

import java.util.Optional;

public interface ProblemTargetDetailPort {

    Optional<ProblemTargetDetailView> findProblemTargetDetail(Long problemId);

    record ProblemTargetDetailView(
            Long problemId,
            String title,
            String status,
            Long problemSetId,
            String problemSetTitle,
            Long createdBy
    ) {
    }
}
