package com.wanted.codebombalms.problems.progress.presentation.response;

import com.wanted.codebombalms.problems.progress.application.usecase.GetProblemProgressUseCase.ProblemProgressItemView;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemProgressItemResponse(
        @Schema(description = "소문제 ID", example = "3001")
        Long problemId,

        @Schema(description = "문제 세트 안에서의 소문제 번호", example = "1")
        Integer problemNumber,

        @Schema(description = "소문제 제목", example = "데이터 행과 열 개수 확인")
        String title,

        @Schema(
                description = "학생 기준 소문제 진행 상태. LOCKED는 아직 열리지 않음, UNSOLVED는 풀이 전, CORRECT는 정답, WRONG은 오답입니다.",
                example = "CORRECT",
                allowableValues = {"LOCKED", "UNSOLVED", "CORRECT", "WRONG"}
        )
        String status,

        @Schema(description = "해당 소문제의 최신 제출 ID. 제출 기록이 없으면 null입니다.", example = "3001", nullable = true)
        Long latestSubmissionId
) {
    public ProblemProgressItemResponse(ProblemProgressItemView item) {
        this(
                item.problemId(),
                item.problemNumber(),
                item.title(),
                item.status(),
                item.latestSubmissionId()
        );
    }
}
