package com.wanted.codebombalms.problems.set.application.service;

import com.wanted.codebombalms.problems.set.application.command.ProblemSetCreateCommandResult;
import com.wanted.codebombalms.problems.set.application.command.RegisterProblemSetCommand;
import com.wanted.codebombalms.problems.set.application.policy.ProblemSetCommandValidationPolicy;
import com.wanted.codebombalms.problems.set.application.usecase.RegisterProblemSetUseCase;
import com.wanted.codebombalms.problems.set.domain.model.ProblemRegistration;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetRegistration;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetRegistrationResult;
import com.wanted.codebombalms.problems.set.domain.repository.ProblemSetManagementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProblemSetRegistrationService implements RegisterProblemSetUseCase {


    private static final String CODE_PROBLEM_TYPE = "CODE";
    private static final int DEFAULT_ATTEMPT_LIMIT = 3;
    private static final boolean DEFAULT_RETRIABLE = true;
    private final ProblemSetCommandValidationPolicy validationPolicy;
    private final ProblemSetManagementRepository problemSetManagementRepository;

    public ProblemSetRegistrationService(
            ProblemSetCommandValidationPolicy validationPolicy,
            ProblemSetManagementRepository problemSetManagementRepository
    ) {
        this.validationPolicy = validationPolicy;
        this.problemSetManagementRepository = problemSetManagementRepository;
    }

    @Override
    @Transactional
    public ProblemSetCreateCommandResult handle(RegisterProblemSetCommand command) {
        validationPolicy.validate(command);

        return toCommandResult(problemSetManagementRepository.createProblemSet(
                toRegistration(command),
                command.createdBy()
        ));
    }

    private ProblemSetRegistration toRegistration(RegisterProblemSetCommand command) {
        List<ProblemRegistration> problems = command.problems()
                .stream()
                .map(problem -> new ProblemRegistration(
                        problem.title(),
                        problem.content(),
                        CODE_PROBLEM_TYPE,
                        command.difficulty(),
                        problem.point(),
                        DEFAULT_ATTEMPT_LIMIT,
                        DEFAULT_RETRIABLE,
                        problem.answer(),
                        problem.hint(),
                        problem.explanation()
                ))
                .toList();

        return new ProblemSetRegistration(
                command.title(),
                command.categoryName(),
                command.difficulty(),
                command.description(),
                problems
        );
    }

    private ProblemSetCreateCommandResult toCommandResult(ProblemSetRegistrationResult result) {
        return new ProblemSetCreateCommandResult(
                result.problemSetId(),
                result.title(),
                result.categoryName(),
                result.totalProblemCount(),
                result.createdProblemCount()
        );
    }
}
