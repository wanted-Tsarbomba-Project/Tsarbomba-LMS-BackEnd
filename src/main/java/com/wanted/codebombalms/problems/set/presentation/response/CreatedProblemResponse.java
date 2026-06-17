package com.wanted.codebombalms.problems.set.presentation.response;

import com.wanted.codebombalms.problems.set.domain.model.CreatedProblemSummary;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreatedProblemResponse(
        @Schema(description = "생성된 소문제 ID", example = "5001")
        Long problemId,

        @Schema(description = "소문제 순서", example = "1")
        Integer problemOrder,

        @Schema(description = "소문제 제목", example = "데이터 행과 열 개수 확인")
        String title
) {
    public static CreatedProblemResponse from(CreatedProblemSummary problem) {
        return new CreatedProblemResponse(
                problem.problemId(),
                problem.problemOrder(),
                problem.title()
        );
    }
}
