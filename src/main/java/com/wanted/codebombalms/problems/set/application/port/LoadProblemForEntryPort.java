package com.wanted.codebombalms.problems.set.application.port;

import com.wanted.codebombalms.problems.set.domain.model.ProblemDetail;

import java.util.List;
import java.util.Optional;

public interface LoadProblemForEntryPort {

    ProblemDetail loadCurrentProblem(Long problemSetId, Integer problemNumber);

    Optional<ProblemDetail> loadLastProblem(Long problemSetId);

    List<ProblemDetail> loadProblems(Long problemSetId);
}
