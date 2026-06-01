package com.wanted.codebombalms.problems.progress.presentation.response;

import com.wanted.codebombalms.problems.progress.application.usecase.GetProblemProgressUseCase.ProblemProgressView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ProblemProgressResponse(
        @Schema(description = "진행 상태를 조회한 문제 세트 ID", example = "3001")
        Long problemSetId,

        @Schema(description = "문제 세트에 포함된 전체 소문제 수", example = "3")
        Integer totalProblemCount,

        @Schema(description = "현재 풀어야 할 소문제 번호. 완료 상태에서는 null일 수 있습니다.", example = "2", nullable = true)
        Integer currentProblemNumber,

        @Schema(description = "현재 풀어야 할 소문제 ID. 완료 상태에서는 null일 수 있습니다.", example = "3002", nullable = true)
        Long currentProblemId,

        @Schema(description = "학생이 해결한 소문제 수", example = "1")
        Integer solvedProblemCount,

        @Schema(description = "문제 세트 내 소문제별 진행 상태 목록")
        List<ProblemProgressItemResponse> problems) {
    public ProblemProgressResponse(ProblemProgressView progress) {
        this(
                progress.problemSetId(),
                progress.totalProblemCount(),
                progress.currentProblemNumber(),
                progress.currentProblemId(),
                progress.solvedProblemCount(),
                progress.problems()
                        .stream()
                        .map(ProblemProgressItemResponse::new)
                        .toList()
        );
    }
}
