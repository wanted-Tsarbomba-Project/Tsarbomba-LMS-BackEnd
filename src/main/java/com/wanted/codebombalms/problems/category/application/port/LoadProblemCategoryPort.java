package com.wanted.codebombalms.problems.category.application.port;

import com.wanted.codebombalms.problems.category.domain.model.ProblemCategory;

import java.util.List;

public interface LoadProblemCategoryPort {

    List<ProblemCategory> loadActiveCategories();
}