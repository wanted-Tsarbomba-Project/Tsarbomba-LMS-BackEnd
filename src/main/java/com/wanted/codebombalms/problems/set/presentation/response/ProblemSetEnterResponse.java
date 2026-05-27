package com.wanted.codebombalms.problems.set.presentation.response;

import com.wanted.codebombalms.problems.set.application.usecase.EnterProblemSetUseCase.ProblemSetEntryView;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemSetEnterResponse(
        @Schema(description = "문제 세트 ID", example = "3001")
        Long problemSetId,

        @Schema(description = "문제 세트 제목", example = "pandas 기초 분석 문제 세트")
        String title,

        @Schema(description = "문제 세트 설명", example = "CSV 데이터를 불러와 기본 정보를 확인하는 문제 세트입니다.")
        String description,

        @Schema(description = "현재 풀어야 할 소문제 번호", example = "1")
        Integer currentProblemNumber,

        @Schema(description = "문제 세트 완료 여부", example = "false")
        Boolean isCompleted,

        @Schema(description = "현재 풀어야 할 문제 정보. 완료 상태에서는 null일 수 있습니다.", nullable = true)
        ProblemDetailResponse problem
) {
    public ProblemSetEnterResponse(ProblemSetEntryView entry) {
        this(
                entry.problemSetId(),
                entry.title(),
                entry.description(),
                entry.currentProblemNumber(),
                entry.isCompleted(),
                entry.problem() == null ? null : new ProblemDetailResponse(entry.problem())
        );
    }
}
