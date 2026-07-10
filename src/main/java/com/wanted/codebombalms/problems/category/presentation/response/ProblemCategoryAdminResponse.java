package com.wanted.codebombalms.problems.category.presentation.response;

import com.wanted.codebombalms.problems.category.application.usecase.ManageProblemCategoriesUseCase.ProblemCategoryAdminView;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemCategoryAdminResponse(
        @Schema(description = "카테고리 ID", example = "1")
        Long categoryId,

        @Schema(description = "카테고리 이름", example = "알고리즘")
        String categoryName,

        @Schema(description = "카테고리 상태", example = "ACTIVE")
        String status
) {
    public ProblemCategoryAdminResponse(ProblemCategoryAdminView view) {
        this(
                view.categoryId(),
                view.categoryName(),
                view.status()
        );
    }
}
