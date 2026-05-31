package com.wanted.codebombalms.problems.set.application.port;

public interface FindOrCreateProblemSetCategoryPort {

    Long findOrCreateActiveCategoryId(String categoryName);
}
