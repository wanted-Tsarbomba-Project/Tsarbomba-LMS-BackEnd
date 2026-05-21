package com.wanted.codebombalms.problems.category.domain.repository;

import com.wanted.codebombalms.problems.category.domain.model.ProblemCategory;

import java.util.List;

public interface ProblemCategoryRepository {

    List<ProblemCategory> loadActiveCategories();
}
