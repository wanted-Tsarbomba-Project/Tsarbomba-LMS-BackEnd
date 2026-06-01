package com.wanted.codebombalms.problems.set.application.port;

import com.wanted.codebombalms.problems.set.domain.model.ProblemDetail;

import java.util.List;
import java.util.Optional;

public interface LoadProblemForEntryPort {

    List<ProblemDetail> loadProblems(Long problemSetId);
}
