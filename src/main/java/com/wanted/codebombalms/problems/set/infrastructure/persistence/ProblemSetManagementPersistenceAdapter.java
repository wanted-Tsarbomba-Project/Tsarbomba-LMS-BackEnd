package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.problems.category.infrastructure.persistence.ProblemCategoryJpaEntity;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.set.application.port.FindOrCreateProblemSetCategoryPort;
import com.wanted.codebombalms.problems.set.application.port.ManageProblemSetHintsPort;
import com.wanted.codebombalms.problems.set.application.port.ManageProblemSetProblemsPort;
import com.wanted.codebombalms.problems.set.domain.model.ProblemModification;
import com.wanted.codebombalms.problems.set.domain.model.ProblemRegistration;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetDeactivationResult;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetModification;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetModificationResult;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetRegistration;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetRegistrationResult;
import com.wanted.codebombalms.problems.set.domain.repository.ProblemSetManagementRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemSetManagementPersistenceAdapter implements ProblemSetManagementRepository {

    private final SpringDataProblemSetRepository problemSetRepository;
    private final FindOrCreateProblemSetCategoryPort findOrCreateProblemSetCategoryPort;
    private final ManageProblemSetProblemsPort manageProblemSetProblemsPort;
    private final ManageProblemSetHintsPort manageProblemSetHintsPort;
    private final EntityManager entityManager;

    @Override
    public ProblemSetRegistrationResult createProblemSet(ProblemSetRegistration registration, Long createdBy) {
        ProblemCategoryJpaEntity category = getCategoryReference(
                findOrCreateProblemSetCategoryPort.findOrCreateActiveCategoryId(registration.categoryName())
        );

        ProblemSetJpaEntity problemSet = new ProblemSetJpaEntity(
                category,
                registration.title(),
                registration.description(),
                registration.difficulty(),
                registration.problems().size(),
                createdBy
        );

        ProblemSetJpaEntity savedProblemSet = problemSetRepository.save(problemSet);
        int createdProblemCount = 0;

        for (int i = 0; i < registration.problems().size(); i++) {
            ProblemRegistration problemCommand = registration.problems().get(i);
            Long problemId = manageProblemSetProblemsPort.createProblem(
                    savedProblemSet.getProblemSetId(),
                    problemCommand,
                    i + 1
            );
            manageProblemSetHintsPort.createHint(problemId, problemCommand.hint());
            createdProblemCount++;
        }

        return new ProblemSetRegistrationResult(
                savedProblemSet.getProblemSetId(),
                savedProblemSet.getTitle(),
                savedProblemSet.getCategory().getCategoryName(),
                savedProblemSet.getTotalProblemCount(),
                createdProblemCount
        );
    }

    @Override
    public ProblemSetModificationResult updateProblemSet(ProblemSetModification modification) {
        ProblemSetJpaEntity problemSet = problemSetRepository.findById(modification.problemSetId())
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));
        ProblemCategoryJpaEntity category = getCategoryReference(
                findOrCreateProblemSetCategoryPort.findOrCreateActiveCategoryId(modification.categoryName())
        );

        problemSet.update(
                category,
                modification.title(),
                modification.description(),
                modification.difficulty()
        );

        int updatedProblemCount = 0;

        for (int i = 0; i < modification.problems().size(); i++) {
            ProblemModification problemCommand = modification.problems().get(i);
            Long problemId = manageProblemSetProblemsPort.updateOrCreateProblem(
                    problemSet.getProblemSetId(),
                    problemCommand,
                    i + 1
            );
            manageProblemSetHintsPort.updateOrCreateHint(problemId, problemCommand.hintId(), problemCommand.hint());
            updatedProblemCount++;
        }

        problemSet.updateTotalProblemCount(modification.problems().size());

        return new ProblemSetModificationResult(
                problemSet.getProblemSetId(),
                problemSet.getTitle(),
                problemSet.getCategory().getCategoryName(),
                updatedProblemCount
        );
    }

    @Override
    public ProblemSetDeactivationResult deactivateProblemSet(Long problemSetId) {
        ProblemSetJpaEntity problemSet = problemSetRepository.findById(problemSetId)
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_SET_NOT_FOUND));

        problemSet.deactivate();
        int deactivatedProblemCount = manageProblemSetProblemsPort.deactivateActiveProblems(problemSetId);

        return new ProblemSetDeactivationResult(
                problemSet.getProblemSetId(),
                "INACTIVE",
                deactivatedProblemCount
        );
    }

    private ProblemCategoryJpaEntity getCategoryReference(Long categoryId) {
        return entityManager.getReference(ProblemCategoryJpaEntity.class, categoryId);
    }
}