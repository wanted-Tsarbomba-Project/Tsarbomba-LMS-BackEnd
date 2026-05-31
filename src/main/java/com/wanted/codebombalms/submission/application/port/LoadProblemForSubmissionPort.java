package com.wanted.codebombalms.submission.application.port;

import java.util.Optional;

public interface LoadProblemForSubmissionPort {

    ProblemForSubmission loadProblemForSubmission(Long problemId);

    Optional<Long> findNextProblemId(Long problemSetId, Integer nextProblemOrder);

    record ProblemForSubmission(
            Long problemId,
            Long problemSetId,
            Integer problemOrder,
            String answer,
            String explanation,
            Integer point,
            Integer attemptLimit,
            Boolean retriable
    ) {
    }
}
