package com.wanted.codebombalms.domain.problems.category.infrastructure.persistence;

import com.wanted.codebombalms.domain.problems.category.domain.model.ProblemCategory;

public class ProblemCategoryMapper {

    private ProblemCategoryMapper() {
    }

    public static ProblemCategory toDomain(
            com.wanted.codebombalms.domain.problems.category.entity.ProblemCategory entity
    ) {
        return ProblemCategory.of(
                entity.getCategoryId(),
                entity.getCategoryName(),
                entity.getDescription(),
                entity.getStatus()
        );
    }
}
