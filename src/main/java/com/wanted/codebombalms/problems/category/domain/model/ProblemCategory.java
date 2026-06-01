package com.wanted.codebombalms.problems.category.domain.model;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.problems.exception.ProblemErrorCode;

public class ProblemCategory {

    private static final ProblemCategoryStatus ACTIVE = ProblemCategoryStatus.ACTIVE;
    private final ProblemCategoryStatus status;

    private static final String DEFAULT_DESCRIPTION = "문제 등록 시 자동 생성된 카테고리입니다.";

    private final Long categoryId;
    private final String categoryName;
    private final String description;

    private ProblemCategory(
            Long categoryId,
            String categoryName,
            String description,
            ProblemCategoryStatus status
    ) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new ValidationException(ProblemErrorCode.PROBLEM_CATEGORY_REQUIRED);
        }

        if (status == null) {
            throw new ValidationException(ProblemErrorCode.INVALID_CATEGORY);
        }

        this.categoryId = categoryId;
        this.categoryName = categoryName.trim();
        this.description = description;
        this.status = status;
    }

    public static ProblemCategory create(String categoryName) {
        return new ProblemCategory(
                null,
                categoryName,
                DEFAULT_DESCRIPTION,
                ACTIVE
        );
    }

    public static ProblemCategory restore(
            Long categoryId,
            String categoryName,
            String description,
            ProblemCategoryStatus status
    ) {
        return new ProblemCategory(categoryId, categoryName, description, status);
    }

    public static ProblemCategory of(
            Long categoryId,
            String categoryName,
            String description,
            ProblemCategoryStatus status
    ) {
        return restore(categoryId, categoryName, description, status);
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

    public ProblemCategoryStatus getStatus() {
        return status;
    }
}
