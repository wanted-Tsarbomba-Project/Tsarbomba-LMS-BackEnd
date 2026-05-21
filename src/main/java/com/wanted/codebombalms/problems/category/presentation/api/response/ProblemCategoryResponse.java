package com.wanted.codebombalms.problems.category.presentation.api.response;

import com.wanted.codebombalms.problems.category.application.usecase.GetProblemCategoriesUseCase.ProblemCategoryView;

public record ProblemCategoryResponse(
        Long categoryId,
        String categoryName,
        String description
) {
    public ProblemCategoryResponse(ProblemCategoryView category) {
        this(
                category.categoryId(),
                category.categoryName(),
                category.description()
        );
    }
}
