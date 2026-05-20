package com.wanted.codebombalms.domain.problems.category.presentation.response;

import com.wanted.codebombalms.domain.problems.category.domain.model.ProblemCategory;

public record ProblemCategoryResponse(
        Long categoryId,
        String categoryName,
        String description
) {
    public ProblemCategoryResponse(ProblemCategory category) {
        this(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getDescription()
        );
    }
}
