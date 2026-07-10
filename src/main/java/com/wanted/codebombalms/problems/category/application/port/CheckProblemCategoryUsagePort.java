package com.wanted.codebombalms.problems.category.application.port;

public interface CheckProblemCategoryUsagePort {

    boolean existsActiveProblemSet(Long categoryId);
}
