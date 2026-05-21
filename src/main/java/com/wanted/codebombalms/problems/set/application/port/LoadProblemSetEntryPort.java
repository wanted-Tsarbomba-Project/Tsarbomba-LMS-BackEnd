package com.wanted.codebombalms.problems.set.application.port;

import com.wanted.codebombalms.problems.set.domain.model.ProblemSetEntry;

public interface LoadProblemSetEntryPort {

    ProblemSetEntry loadProblemSetEntry(Long problemSetId);
}
