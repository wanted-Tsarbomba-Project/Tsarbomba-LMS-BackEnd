package com.wanted.codebombalms.problems.category.application.port;

import com.wanted.codebombalms.problems.category.domain.model.ProblemCategory;

import java.util.List;

public interface ManageProblemCategoryPort {

    List<ProblemCategory> loadAllCategories();

    boolean existsByCategoryName(String categoryName);

    boolean existsByCategoryNameAndCategoryIdNot(String categoryName, Long categoryId);

    ProblemCategory create(String categoryName);

    ProblemCategory updateName(Long categoryId, String categoryName);

    ProblemCategory deactivate(Long categoryId);
}
