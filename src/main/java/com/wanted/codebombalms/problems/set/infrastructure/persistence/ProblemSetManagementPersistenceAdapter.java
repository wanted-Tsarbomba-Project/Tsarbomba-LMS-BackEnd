package com.wanted.codebombalms.problems.set.infrastructure.persistence;

import com.wanted.codebombalms.problems.category.infrastructure.persistence.ProblemCategoryJpaEntity;
import com.wanted.codebombalms.problems.category.domain.model.ProblemCategory;
import com.wanted.codebombalms.problems.category.infrastructure.persistence.ProblemCategoryMapper;
import com.wanted.codebombalms.problems.category.infrastructure.persistence.SpringDataProblemCategoryRepository;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import com.wanted.codebombalms.problems.hint.infrastructure.persistence.ProblemHintJpaEntity;
import com.wanted.codebombalms.problems.hint.infrastructure.persistence.SpringDataProblemHintRepository;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.ProblemJpaEntity;
import com.wanted.codebombalms.problems.problem.infrastructure.persistence.SpringDataProblemRepository;
import com.wanted.codebombalms.problems.set.domain.model.ProblemModification;
import com.wanted.codebombalms.problems.set.domain.model.ProblemRegistration;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetDeactivationResult;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetModification;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetModificationResult;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetRegistration;
import com.wanted.codebombalms.problems.set.domain.model.ProblemSetRegistrationResult;
import com.wanted.codebombalms.problems.set.domain.repository.ProblemSetManagementRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProblemSetManagementPersistenceAdapter implements ProblemSetManagementRepository {

    private final SpringDataProblemCategoryRepository problemCategoryRepository;
    private final SpringDataProblemSetRepository problemSetRepository;
    private final SpringDataProblemRepository problemRepository;
    private final SpringDataProblemHintRepository problemHintRepository;

    public ProblemSetManagementPersistenceAdapter(
            SpringDataProblemCategoryRepository problemCategoryRepository,
            SpringDataProblemSetRepository problemSetRepository,
            SpringDataProblemRepository problemRepository,
            SpringDataProblemHintRepository problemHintRepository
    ) {
        this.problemCategoryRepository = problemCategoryRepository;
        this.problemSetRepository = problemSetRepository;
        this.problemRepository = problemRepository;
        this.problemHintRepository = problemHintRepository;
    }

    @Override
    public ProblemSetRegistrationResult createProblemSet(ProblemSetRegistration registration, Long createdBy) {
        ProblemCategoryJpaEntity category = findOrCreateActiveCategory(registration.categoryName());

        ProblemSetJpaEntity problemSet = new ProblemSetJpaEntity(
                category,
                registration.title(),
                registration.description(),
                "EASY",
                registration.problems().size(),
                createdBy
        );

        ProblemSetJpaEntity savedProblemSet = problemSetRepository.save(problemSet);
        int createdProblemCount = 0;

        for (int i = 0; i < registration.problems().size(); i++) {
            ProblemRegistration problemCommand = registration.problems().get(i);
            ProblemJpaEntity savedProblem = problemRepository.save(toProblemEntity(
                    savedProblemSet,
                    problemCommand,
                    i + 1
            ));
            createHint(savedProblem, problemCommand.hint());
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
        ProblemCategoryJpaEntity category = findOrCreateActiveCategory(modification.categoryName());

        problemSet.update(
                category,
                modification.title(),
                modification.description()
        );

        int updatedProblemCount = 0;

        for (int i = 0; i < modification.problems().size(); i++) {
            ProblemModification problemCommand = modification.problems().get(i);
            ProblemJpaEntity problem = updateOrCreateProblem(problemSet, problemCommand, i + 1);
            updateOrCreateHint(problem, problemCommand);
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
        List<ProblemJpaEntity> problems = problemRepository.findByProblemSet_ProblemSetIdAndStatusOrderByProblemOrderAsc(
                problemSetId,
                "ACTIVE"
        );

        problemSet.deactivate();
        problems.forEach(ProblemJpaEntity::deactivate);

        return new ProblemSetDeactivationResult(
                problemSet.getProblemSetId(),
                "INACTIVE",
                problems.size()
        );
    }

    private ProblemCategoryJpaEntity findOrCreateActiveCategory(String categoryName) {
        ProblemCategory category = ProblemCategory.create(categoryName);

        return problemCategoryRepository.findByCategoryNameAndStatus(
                        category.getCategoryName(),
                        category.getStatus()
                )
                .orElseGet(() -> problemCategoryRepository.save(
                        ProblemCategoryMapper.toEntity(category)
                ));
    }

    private ProblemJpaEntity toProblemEntity(
            ProblemSetJpaEntity problemSet,
            ProblemRegistration command,
            Integer problemOrder
    ) {
        return new ProblemJpaEntity(
                problemSet,
                command.title(),
                command.content(),
                command.answer(),
                command.explanation(),
                score(command.point()),
                problemOrder
        );
    }

    private ProblemJpaEntity toProblemEntity(
            ProblemSetJpaEntity problemSet,
            ProblemModification command,
            Integer problemOrder
    ) {
        return new ProblemJpaEntity(
                problemSet,
                command.title(),
                command.content(),
                command.answer(),
                command.explanation(),
                score(command.point()),
                problemOrder
        );
    }

    private ProblemJpaEntity updateOrCreateProblem(
            ProblemSetJpaEntity problemSet,
            ProblemModification command,
            Integer problemOrder
    ) {
        if (command.problemId() == null) {
            return problemRepository.save(toProblemEntity(problemSet, command, problemOrder));
        }

        ProblemJpaEntity problem = problemRepository
                .findByProblemIdAndProblemSet_ProblemSetId(command.problemId(), problemSet.getProblemSetId())
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        problem.update(
                command.title(),
                command.content(),
                command.answer(),
                command.explanation(),
                score(command.point())
        );

        return problem;
    }

    private void createHint(ProblemJpaEntity problem, String hintContent) {
        if (hintContent == null || hintContent.isBlank()) {
            return;
        }

        problemHintRepository.save(new ProblemHintJpaEntity(problem, 1, hintContent));
    }

    private void updateOrCreateHint(ProblemJpaEntity problem, ProblemModification command) {
        if (command.hint() == null || command.hint().isBlank()) {
            return;
        }

        if (command.hintId() == null) {
            createHint(problem, command.hint());
            return;
        }

        ProblemHintJpaEntity hint = problemHintRepository
                .findByHintIdAndProblem_ProblemId(command.hintId(), problem.getProblemId())
                .orElseThrow(() -> new NotFoundException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        hint.update(command.hint());
    }

    private int score(Integer point) {
        return point == null ? 0 : point;
    }
}
