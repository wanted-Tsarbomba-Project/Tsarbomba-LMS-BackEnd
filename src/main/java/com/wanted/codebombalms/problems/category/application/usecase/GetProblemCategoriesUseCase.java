package com.wanted.codebombalms.problems.category.application.usecase;

import java.util.List;

public interface GetProblemCategoriesUseCase {

    List<ProblemCategoryView> handle();

    record ProblemCategoryView(
            Long categoryId,
            String categoryName,
            String description
    ) {
    }
}
