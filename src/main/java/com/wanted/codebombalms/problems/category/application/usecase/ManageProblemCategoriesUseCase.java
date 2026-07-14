package com.wanted.codebombalms.problems.category.application.usecase;

import java.util.List;

public interface ManageProblemCategoriesUseCase {

    List<ProblemCategoryAdminView> findCategories();

    ProblemCategoryAdminView create(String categoryName);

    ProblemCategoryAdminView updateName(Long categoryId, String categoryName);

    ProblemCategoryAdminView deactivate(Long categoryId);

    ProblemCategoryAdminView activate(Long categoryId);

    record ProblemCategoryAdminView(
            Long categoryId,
            String categoryName,
            String status
    ) {
    }
}
