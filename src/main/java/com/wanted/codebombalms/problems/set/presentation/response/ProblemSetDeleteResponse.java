package com.wanted.codebombalms.problems.set.presentation.response;

import com.wanted.codebombalms.problems.set.application.command.ProblemSetDeleteCommandResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemSetDeleteResponse(
        @Schema(description = "삭제 또는 비활성화된 문제 세트 ID", example = "3001")
        Long problemSetId,

        @Schema(description = "변경된 문제 세트 상태", example = "INACTIVE")
        String status,

        @Schema(description = "비활성화된 소문제 수", example = "2")
        int deactivatedProblemCount
) {
    public ProblemSetDeleteResponse(ProblemSetDeleteCommandResult result) {
        this(
                result.problemSetId(),
                result.status(),
                result.deactivatedProblemCount()
        );
    }
}