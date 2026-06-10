package com.wanted.codebombalms.problems.set.application.port;

import com.wanted.codebombalms.problems.set.domain.model.ProblemModification;
import com.wanted.codebombalms.problems.set.domain.model.ProblemRegistration;

import java.util.List;
import java.util.Set;

public interface ManageProblemSetProblemsPort {

    Long createProblem(Long problemSetId, ProblemRegistration command, Integer problemOrder);

    Long updateOrCreateProblem(Long problemSetId, ProblemModification command, Integer problemOrder);

    List<Long> deactivateProblemsNotIn(Long problemSetId, Set<Long> retainedProblemIds);

    List<Long> deactivateActiveProblems(Long problemSetId);
}
