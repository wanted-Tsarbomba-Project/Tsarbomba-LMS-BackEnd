package com.wanted.codebombalms.problems.set.presentation.response;

import com.wanted.codebombalms.problems.set.application.usecase.EnterProblemSetUseCase.ProblemSetEntryView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ProblemSetEnterResponse(
        @Schema(description = "문제 세트 ID", example = "3001")
        Long problemSetId,

        @Schema(description = "문제 세트 제목", example = "pandas 기초 분석 문제 세트")
        String title,

        @Schema(description = "문제 세트 설명", example = "CSV 데이터를 활용한 코드 실행형 문제 세트입니다.")
        String description,

        @Schema(description = "현재 풀어야 하는 문제 번호", example = "1")
        Integer currentProblemNumber,

        @Schema(description = "현재 풀어야 하는 문제 ID", example = "3001")
        Long currentProblemId,

        @Schema(description = "전체 소문제 수", example = "3")
        Integer totalProblemCount,

        @Schema(description = "정답 처리된 소문제 수", example = "1")
        Integer solvedProblemCount,

        @Schema(description = "문제 세트 완료 여부", example = "false")
        Boolean isCompleted,

        @Schema(description = "문제 세트에 포함된 전체 소문제 목록")
        List<ProblemDetailResponse> problems
) {
    public ProblemSetEnterResponse(ProblemSetEntryView entry) {
        this(
                entry.problemSetId(),
                entry.title(),
                entry.description(),
                entry.currentProblemNumber(),
                entry.currentProblemId(),
                entry.totalProblemCount(),
                entry.solvedProblemCount(),
                entry.isCompleted(),
                entry.problems().stream()
                        .map(ProblemDetailResponse::new)
                        .toList()
        );
    }
}
