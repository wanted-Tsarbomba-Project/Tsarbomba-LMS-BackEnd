package com.wanted.codebombalms.problems.progress.presentation.response;

import com.wanted.codebombalms.problems.progress.application.usecase.GetProblemProgressUseCase.ProblemProgressItemView;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemProgressItemResponse(
        @Schema(description = "소문제 ID", example = "3001")
        Long problemId,

        @Schema(description = "문제 세트 내 소문제 번호", example = "1")
        Integer problemNumber,

        @Schema(
                description = "학생 기준 소문제 진행 상태. LOCKED는 아직 열리지 않음, OPEN은 현재 풀이 가능, SOLVED는 해결 완료를 의미합니다.",
                example = "OPEN",
                allowableValues = {"LOCKED", "OPEN", "SOLVED"}
        )
        String status
)
{
    public ProblemProgressItemResponse(ProblemProgressItemView item) {
        this(
                item.problemId(),
                item.problemNumber(),
                item.status()
        );
    }
}
