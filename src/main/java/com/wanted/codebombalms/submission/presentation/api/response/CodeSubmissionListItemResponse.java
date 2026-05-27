package com.wanted.codebombalms.submission.presentation.api.response;

import com.wanted.codebombalms.submission.application.usecase.CodeSubmissionListQueryUseCase.CodeSubmissionListItemView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CodeSubmissionListItemResponse(
        @Schema(description = "제출 ID", example = "3001")
        Long submissionId,

        @Schema(description = "문제 ID", example = "3001")
        Long problemId,

        @Schema(description = "정답 여부", example = "true")
        Boolean isCorrect,

        @Schema(description = "통과한 테스트케이스 수", example = "2")
        Integer passedTestCount,

        @Schema(description = "전체 테스트케이스 수", example = "2")
        Integer totalTestCount,

        @Schema(description = "채점 실행 상태", example = "SUCCESS")
        String executionStatus,

        @Schema(description = "제출 시각", example = "2026-05-27T11:30:00")
        LocalDateTime submittedAt
) {

    public CodeSubmissionListItemResponse(CodeSubmissionListItemView item) {
        this(
                item.submissionId(),
                item.problemId(),
                item.correct(),
                item.passedTestCount(),
                item.totalTestCount(),
                item.executionStatus(),
                item.submittedAt()
        );
    }
}
