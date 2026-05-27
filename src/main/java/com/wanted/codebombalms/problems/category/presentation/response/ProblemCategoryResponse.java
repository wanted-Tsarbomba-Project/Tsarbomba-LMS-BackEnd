package com.wanted.codebombalms.problems.category.presentation.response;

import com.wanted.codebombalms.problems.category.application.usecase.GetProblemCategoriesUseCase.ProblemCategoryView;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemCategoryResponse(
        @Schema(description = "문제 카테고리 ID", example = "3001")
        Long categoryId,

        @Schema(description = "문제 카테고리명", example = "Python 데이터 분석")
        String categoryName,

        @Schema(description = "문제 카테고리 설명", example = "Python과 pandas를 활용한 코드 실행형 문제 분야입니다.", nullable = true)
        String description
) {
    public ProblemCategoryResponse(ProblemCategoryView category) {
        this(
                category.categoryId(),
                category.categoryName(),
                category.description()
        );
    }
}
