package com.wanted.codebombalms.problems.set.presentation.response;

import com.wanted.codebombalms.problems.set.application.usecase.EnterProblemSetUseCase.ProblemDetailItemView;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemDetailResponse(
        @Schema(description = "문제 ID", example = "3001")
        Long problemId,

        @Schema(description = "문제 세트 안에서의 소문제 번호", example = "1")
        Integer problemNumber,

        @Schema(description = "소문제 제목", example = "데이터 행과 열 개수 확인")
        String title,

        @Schema(description = "소문제 내용", example = "DataFrame의 행과 열 개수를 확인하세요.")
        String content,

        @Schema(description = "문제 유형", example = "CODE")
        String problemType,

        @Schema(description = "정답 시 지급 포인트", example = "10")
        Integer point,

        @Schema(description = "코드 에디터에 표시할 시작 코드", nullable = true)
        String startCode,

        @Schema(
                description = "학생 기준 소문제 상태",
                example = "LOCKED",
                allowableValues = {"LOCKED", "UNSOLVED", "CORRECT", "WRONG"}
        )
        String status,

        @Schema(description = "가장 최근 제출 ID", example = "3021", nullable = true)
        Long latestSubmissionId
) {
    public ProblemDetailResponse(ProblemDetailItemView problem) {
        this(
                problem.problemId(),
                problem.problemNumber(),
                problem.title(),
                problem.content(),
                problem.problemType(),
                problem.point(),
                problem.startCode(),
                problem.status(),
                problem.latestSubmissionId()
        );
    }
}
