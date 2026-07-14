package com.wanted.codebombalms.problems.category.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.category.application.port.CheckProblemCategoryUsagePort;
import com.wanted.codebombalms.problems.category.application.port.ManageProblemCategoryPort;
import com.wanted.codebombalms.problems.category.application.usecase.ManageProblemCategoriesUseCase;
import com.wanted.codebombalms.problems.category.domain.model.ProblemCategory;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemCategoryManagementService implements ManageProblemCategoriesUseCase {

    private final ManageProblemCategoryPort manageProblemCategoryPort;
    private final CheckProblemCategoryUsagePort checkProblemCategoryUsagePort;

    @Override
    public List<ProblemCategoryAdminView> findCategories() {
        return manageProblemCategoryPort.loadAllCategories()
                .stream()
                .map(this::toView)
                .toList();
    }

    @Override
    @Transactional
    public ProblemCategoryAdminView create(String categoryName) {
        String normalizedCategoryName = normalizeCategoryName(categoryName);

        if (manageProblemCategoryPort.existsActiveByCategoryName(normalizedCategoryName)) {
            throw new ConflictException(ProblemErrorCode.CATEGORY_ALREADY_EXISTS);
        }

        return toView(manageProblemCategoryPort.create(normalizedCategoryName));
    }

    @Override
    @Transactional
    public ProblemCategoryAdminView updateName(Long categoryId, String categoryName) {
        String normalizedCategoryName = normalizeCategoryName(categoryName);

        if (manageProblemCategoryPort.existsActiveByCategoryNameAndCategoryIdNot(
                normalizedCategoryName,
                categoryId
        )) {
            throw new ConflictException(ProblemErrorCode.CATEGORY_ALREADY_EXISTS);
        }

        return toView(manageProblemCategoryPort.updateName(categoryId, normalizedCategoryName));
    }

    @Override
    @Transactional
    public ProblemCategoryAdminView deactivate(Long categoryId) {
        if (checkProblemCategoryUsagePort.existsActiveProblemSet(categoryId)) {
            throw new ConflictException(ProblemErrorCode.INVALID_CATEGORY);
        }

        return toView(manageProblemCategoryPort.deactivate(categoryId));
    }

    private String normalizeCategoryName(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_CATEGORY_REQUIRED);
        }

        return categoryName.trim();
    }

    @Override
    @Transactional
    public ProblemCategoryAdminView activate(Long categoryId) {
        ProblemCategory category = manageProblemCategoryPort.loadCategory(categoryId);

        if (manageProblemCategoryPort.existsActiveByCategoryNameAndCategoryIdNot(
                category.getCategoryName(),
                categoryId
        )) {
            throw new ConflictException(ProblemErrorCode.CATEGORY_ALREADY_EXISTS);
        }

        return toView(manageProblemCategoryPort.activate(categoryId));
    }

    private ProblemCategoryAdminView toView(ProblemCategory category) {
        return new ProblemCategoryAdminView(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getStatus().name()
        );
    }
}
