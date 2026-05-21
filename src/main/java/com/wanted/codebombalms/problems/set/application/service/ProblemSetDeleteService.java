package com.wanted.codebombalms.problems.set.application.service;

import com.wanted.codebombalms.problems.set.application.command.DeleteProblemSetCommand;
import com.wanted.codebombalms.problems.set.application.command.ProblemSetDeleteCommandResult;
import com.wanted.codebombalms.problems.set.application.policy.ProblemSetDeletionPolicy;
import com.wanted.codebombalms.problems.set.application.usecase.DeleteProblemSetUseCase;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetDeactivationResult;
import com.wanted.codebombalms.problems.set.domain.repository.ProblemSetManagementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProblemSetDeleteService implements DeleteProblemSetUseCase {

    private final ProblemSetDeletionPolicy deletionPolicy;
    private final ProblemSetManagementRepository problemSetManagementRepository;

    public ProblemSetDeleteService(
            ProblemSetDeletionPolicy deletionPolicy,
            ProblemSetManagementRepository problemSetManagementRepository
    ) {
        this.deletionPolicy = deletionPolicy;
        this.problemSetManagementRepository = problemSetManagementRepository;
    }

    @Override
    @Transactional
    public ProblemSetDeleteCommandResult handle(DeleteProblemSetCommand command) {
        boolean hasSubmission = problemSetManagementRepository.existsSubmission(command.problemSetId());

        deletionPolicy.validate(hasSubmission, command.force());

        return toCommandResult(problemSetManagementRepository.deactivateProblemSet(command.problemSetId()));
    }

    private ProblemSetDeleteCommandResult toCommandResult(ProblemSetDeactivationResult result) {
        return new ProblemSetDeleteCommandResult(
                result.problemSetId(),
                result.status(),
                result.deactivatedProblemCount()
        );
    }
}
