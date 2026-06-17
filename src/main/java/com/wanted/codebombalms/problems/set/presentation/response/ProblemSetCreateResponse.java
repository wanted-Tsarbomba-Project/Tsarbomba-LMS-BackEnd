package com.wanted.codebombalms.problems.set.presentation.response;

import com.wanted.codebombalms.problems.set.application.command.ProblemSetCreateCommandResult;
import com.wanted.codebombalms.problems.set.domain.model.CreatedProblemSummary;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ProblemSetCreateResponse(
        @Schema(description = "생성된 문제세트 ID", example = "4001")
        Long problemSetId,

        @Schema(description = "문제세트 제목", example = "pandas 기초 분석 문제 세트")
        String title,

        @Schema(description = "카테고리명", example = "Python 데이터 분석")
        String categoryName,

        @Schema(description = "전체 소문제 개수", example = "2")
        Integer totalProblemCount,

        @Schema(description = "생성된 소문제 개수", example = "2")
        Integer createdProblemCount,

        @Schema(description = "생성된 테스트케이스 개수", example = "4")
        Integer createdTestCaseCount,

        @Schema(description = "생성된 소문제 목록")
        List<CreatedProblemResponse> problems
) {
    public ProblemSetCreateResponse(ProblemSetCreateCommandResult result) {
        this(
                result.problemSetId(),
                result.title(),
                result.categoryName(),
                result.totalProblemCount(),
                result.createdProblemCount(),
                result.createdTestCaseCount(),
                result.problems().stream()
                        .map(CreatedProblemResponse::from)
                        .toList()
        );
    }

    public record CreatedProblemResponse(
            @Schema(description = "생성된 소문제 ID", example = "5001")
            Long problemId,

            @Schema(description = "소문제 순서", example = "1")
            Integer problemOrder,

            @Schema(description = "소문제 제목", example = "데이터 행과 열 개수 확인")
            String title
    ) {
        private static CreatedProblemResponse from(CreatedProblemSummary problem) {
            return new CreatedProblemResponse(
                    problem.problemId(),
                    problem.problemOrder(),
                    problem.title()
            );
        }
    }
}
