package com.wanted.codebombalms.problems.set.presentation.response;

import com.wanted.codebombalms.problems.set.application.usecase.GetProblemSetsUseCase.ProblemSetSummaryView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ProblemSetListResponse(
        @Schema(description = "문제 세트 ID", example = "3001")
        Long problemSetId,

        @Schema(description = "목록에서 표시할 문제 세트 번호", example = "1")
        Integer problemNumber,

        @Schema(description = "문제 세트 제목", example = "pandas 기초 분석 문제 세트")
        String title,

        @Schema(description = "문제 세트 설명", example = "CSV 데이터를 불러와 기본 정보를 확인하는 문제 세트입니다.")
        String description,

        @Schema(description = "문제 세트 난이도", example = "EASY")
        String difficulty,

        @Schema(description = "문제 세트 정답률", example = "75.5", nullable = true)
        Double accuracyRate,

        @Schema(description = "문제세트를 완료한 사용자 수", example = "18")
        Integer completedUserCount,

        @Schema(description = "문제세트에 진입한 사용자 수", example = "42")
        Integer startedUserCount,
        @Schema(
                description = "로그인 사용자 기준 문제세트 풀이 상태",
                example = "IN_PROGRESS",
                allowableValues = {"NOT_STARTED", "IN_PROGRESS", "COMPLETED"}
        )
        String completionStatus,
        @Schema(description = "문제 세트 생성일", example = "2026-05-27T10:00:00")
        LocalDateTime createdAt
) {
    public ProblemSetListResponse(ProblemSetSummaryView problemSet) {
        this(
                problemSet.problemSetId(),
                problemSet.problemNumber(),
                problemSet.title(),
                problemSet.description(),
                problemSet.difficulty(),
                problemSet.accuracyRate(),
                problemSet.completedUserCount(),
                problemSet.startedUserCount(),
                problemSet.completionStatus(),
                problemSet.createdAt()
        );
    }
}
