package com.wanted.codebombalms.domain.problems.category.application.usecase;

import com.wanted.codebombalms.domain.problems.category.domain.model.ProblemCategory;

import java.util.List;

public interface GetProblemCategoriesUseCase {

    List<ProblemCategory> getActiveCategories();
}
