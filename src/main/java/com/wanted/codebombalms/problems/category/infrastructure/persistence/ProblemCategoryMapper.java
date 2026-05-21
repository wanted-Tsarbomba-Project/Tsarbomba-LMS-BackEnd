package com.wanted.codebombalms.problems.category.infrastructure.persistence;

import com.wanted.codebombalms.problems.category.domain.model.ProblemCategory;

public class ProblemCategoryMapper {

    private ProblemCategoryMapper() {
    }

    public static ProblemCategory toDomain(
            ProblemCategoryJpaEntity entity
    ) {
        return ProblemCategory.restore(
                entity.getCategoryId(),
                entity.getCategoryName(),
                entity.getDescription(),
                entity.getStatus()
        );
    }

    public static ProblemCategoryJpaEntity toEntity(ProblemCategory category) {
        return new ProblemCategoryJpaEntity(
                category.getCategoryName(),
                category.getDescription()
        );
    }
}
