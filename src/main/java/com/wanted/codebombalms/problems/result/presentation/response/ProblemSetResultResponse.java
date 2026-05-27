package com.wanted.codebombalms.problems.result.presentation.response;

import com.wanted.codebombalms.problems.result.application.usecase.GetProblemSetResultUseCase.ProblemSetResultView;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ProblemSetResultResponse(
        @Schema(description = "문제 세트 ID", example = "3001")
        Long problemSetId,

        @Schema(description = "문제 세트 제목", example = "pandas 기초 분석 문제 세트")
        String title,

        @Schema(description = "학생의 문제 세트 완료 여부", example = "true")
        Boolean isCompleted,

        @Schema(description = "문제 세트 정답률. 예: 75.0은 75%를 의미", example = "75.0")
        Double accuracyRate,

        @Schema(description = "문제 세트를 완료한 전체 사용자 수", example = "12")
        Integer totalCompletedUserCount,

        @Schema(description = "문제 세트를 정답으로 완료한 사용자 수", example = "9")
        Integer correctCompletedUserCount,

        @Schema(description = "문제별 제출 결과 목록")
        List<ProblemSubmissionResultResponse> submissions)
{
    public ProblemSetResultResponse(ProblemSetResultView result) {
        this(
                result.problemSetId(),
                result.title(),
                result.isCompleted(),
                result.accuracyRate(),
                result.totalCompletedUserCount(),
                result.correctCompletedUserCount(),
                result.submissions()
                        .stream()
                        .map(ProblemSubmissionResultResponse::new)
                        .toList()
        );
    }
}
