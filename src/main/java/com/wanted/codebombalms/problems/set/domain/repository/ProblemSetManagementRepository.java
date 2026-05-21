package com.wanted.codebombalms.problems.set.domain.repository;

import com.wanted.codebombalms.problems.set.domain.model.ProblemSetDeactivationResult;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetModification;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetModificationResult;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetRegistration;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetRegistrationResult;

public interface ProblemSetManagementRepository {

    ProblemSetRegistrationResult createProblemSet(ProblemSetRegistration registration, Long createdBy);

    ProblemSetModificationResult updateProblemSet(ProblemSetModification modification);

    boolean existsSubmission(Long problemSetId);

    ProblemSetDeactivationResult deactivateProblemSet(Long problemSetId);
}
