package com.wanted.codebombalms.problems.set.presentation.response;

import com.wanted.codebombalms.problems.set.application.command.ProblemSetUpdateCommandResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemSetUpdateResponse(
        @Schema(description = "수정된 문제 세트 ID", example = "3001")
        Long problemSetId,

        @Schema(description = "문제 세트 제목", example = "pandas 기초 분석 문제 세트")
        String title,

        @Schema(description = "카테고리명", example = "Python 데이터 분석")
        String categoryName,

        @Schema(description = "수정된 소문제 수", example = "2")
        Integer updatedProblemCount
) {
    public ProblemSetUpdateResponse(ProblemSetUpdateCommandResult result) {
        this(
                result.problemSetId(),
                result.title(),
                result.categoryName(),
                result.updatedProblemCount()
        );
    }
}
