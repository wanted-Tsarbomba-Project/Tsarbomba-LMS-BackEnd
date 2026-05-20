package com.wanted.codebombalms.domain.problems.category.domain.model;

public class ProblemCategory {

    private final Long categoryId;
    private final String categoryName;
    private final String description;
    private final String status;

    private ProblemCategory(
            Long categoryId,
            String categoryName,
            String description,
            String status
    ) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.status = status;
    }

    public static ProblemCategory of(
            Long categoryId,
            String categoryName,
            String description,
            String status
    ) {
        return new ProblemCategory(categoryId, categoryName, description, status);
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }
}
