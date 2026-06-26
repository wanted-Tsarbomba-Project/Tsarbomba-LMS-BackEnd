package com.wanted.codebombalms.problems.set.presentation.response;

import com.wanted.codebombalms.problems.set.application.usecase.GetProblemSetsUseCase.ProblemSetPageView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ProblemSetPageResponse(
        @Schema(description = "문제 세트 목록")
        List<ProblemSetListResponse> content,

        @Schema(description = "현재 페이지 번호(0부터 시작)", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "20")
        int size,

        @Schema(description = "전체 문제 세트 수", example = "125")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "7")
        int totalPages,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
    public static ProblemSetPageResponse from(ProblemSetPageView page) {
        return new ProblemSetPageResponse(
                page.content()
                        .stream()
                        .map(ProblemSetListResponse::new)
                        .toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.hasNext()
        );
    }
}
