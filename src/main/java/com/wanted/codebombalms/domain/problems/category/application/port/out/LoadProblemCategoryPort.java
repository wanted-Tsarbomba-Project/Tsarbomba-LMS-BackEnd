package com.wanted.codebombalms.domain.problems.category.application.port.out;

import com.wanted.codebombalms.domain.problems.category.domain.model.ProblemCategory;

import java.util.List;

public interface LoadProblemCategoryPort {

    List<ProblemCategory> loadActiveCategories();
}
