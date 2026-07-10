package com.wanted.codebombalms.problems.category.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ProblemCategoryUpdateRequest(
        @Schema(description = "수정할 문제 카테고리 이름", example = "Python 기초")
        @NotBlank(message = "카테고리 이름은 필수입니다.")
        String categoryName
) {
}
