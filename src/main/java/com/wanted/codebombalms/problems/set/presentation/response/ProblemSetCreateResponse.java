package com.wanted.codebombalms.problems.set.presentation.response;

import com.wanted.codebombalms.problems.set.application.command.ProblemSetCreateCommandResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemSetCreateResponse(
        @Schema(description = "생성된 문제 세트 ID", example = "3001")
        Long problemSetId,

        @Schema(description = "문제 세트 제목", example = "pandas 기초 분석 문제 세트")
        String title,

        @Schema(description = "카테고리명", example = "Python 데이터 분석")
        String categoryName,

        @Schema(description = "전체 소문제 수", example = "2")
        Integer totalProblemCount,

        @Schema(description = "생성된 소문제 수", example = "2")
        Integer createdProblemCount,

        @Schema(description = "생성된 테스트케이스 개수", example = "4")
        Integer createdTestCaseCount
) {
    public ProblemSetCreateResponse(ProblemSetCreateCommandResult result) {
        this(
                result.problemSetId(),
                result.title(),
                result.categoryName(),
                result.totalProblemCount(),
                result.createdProblemCount(),
                result.createdTestCaseCount()
        );
    }
}
