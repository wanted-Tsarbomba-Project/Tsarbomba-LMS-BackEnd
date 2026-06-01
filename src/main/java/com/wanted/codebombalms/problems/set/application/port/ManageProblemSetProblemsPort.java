package com.wanted.codebombalms.problems.set.application.port;

import com.wanted.codebombalms.problems.set.domain.model.ProblemModification;
import com.wanted.codebombalms.problems.set.domain.model.ProblemRegistration;

public interface ManageProblemSetProblemsPort {

    Long createProblem(Long problemSetId, ProblemRegistration command, Integer problemOrder);

    Long updateOrCreateProblem(Long problemSetId, ProblemModification command, Integer problemOrder);

    int deactivateActiveProblems(Long problemSetId);
}