package com.wanted.codebombalms.problems.set.application.service;

import com.wanted.codebombalms.problems.set.application.command.ProblemSetUpdateCommandResult;
import com.wanted.codebombalms.problems.set.application.command.UpdateProblemSetCommand;
import com.wanted.codebombalms.problems.set.application.policy.ProblemSetCommandValidationPolicy;
import com.wanted.codebombalms.problems.set.application.usecase.UpdateProblemSetUseCase;
import com.wanted.codebombalms.problems.set.domain.model.ProblemModification;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetModification;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetModificationResult;
import com.wanted.codebombalms.problems.set.domain.repository.ProblemSetManagementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProblemSetUpdateService implements UpdateProblemSetUseCase {

    private final ProblemSetCommandValidationPolicy validationPolicy;
    private final ProblemSetManagementRepository problemSetManagementRepository;
    private static final String CODE_PROBLEM_TYPE = "CODE";
    private static final int DEFAULT_ATTEMPT_LIMIT = 3;
    private static final boolean DEFAULT_RETRIABLE = true;

    public ProblemSetUpdateService(
            ProblemSetCommandValidationPolicy validationPolicy,
            ProblemSetManagementRepository problemSetManagementRepository
    ) {
        this.validationPolicy = validationPolicy;
        this.problemSetManagementRepository = problemSetManagementRepository;
    }

    @Override
    @Transactional
    public ProblemSetUpdateCommandResult handle(UpdateProblemSetCommand command) {
        validationPolicy.validate(command);

        return toCommandResult(problemSetManagementRepository.updateProblemSet(toModification(command)));
    }

    private ProblemSetModification toModification(UpdateProblemSetCommand command) {
        List<ProblemModification> problems = command.problems()
                .stream()
                .map(problem -> new ProblemModification(
                        problem.problemId(),
                        problem.title(),
                        problem.content(),
                        CODE_PROBLEM_TYPE,
                        command.difficulty(),
                        problem.point(),
                        DEFAULT_ATTEMPT_LIMIT,
                        DEFAULT_RETRIABLE,
                        problem.answer(),
                        problem.hintId(),
                        problem.hint(),
                        problem.explanation()
                ))
                .toList();

        return new ProblemSetModification(
                command.problemSetId(),
                command.title(),
                command.categoryName(),
                command.difficulty(),
                command.description(),
                problems
        );
    }

    private ProblemSetUpdateCommandResult toCommandResult(ProblemSetModificationResult result) {
        return new ProblemSetUpdateCommandResult(
                result.problemSetId(),
                result.title(),
                result.categoryName(),
                result.updatedProblemCount()
        );
    }
}
