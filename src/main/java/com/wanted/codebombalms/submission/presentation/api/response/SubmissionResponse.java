package com.wanted.codebombalms.submission.presentation.api.response;

import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase.SubmissionView;
import io.swagger.v3.oas.annotations.media.Schema;

public record SubmissionResponse(
        @Schema(description = "생성된 제출 ID", example = "3001")
        Long submissionId,

        @Schema(description = "제출한 문제 ID", example = "3001")
        Long problemId,

        @Schema(description = "정답 여부", example = "true")
        Boolean isCorrect,

        @Schema(description = "통과한 테스트케이스 수", example = "2")
        Integer passedTestCount,

        @Schema(description = "전체 테스트케이스 수", example = "2")
        Integer totalTestCount,

        @Schema(description = "채점 실행 상태", example = "SUCCESS")
        String executionStatus,

        @Schema(description = "채점 실패 또는 오답 메시지. 성공한 경우 null", example = "2개의 테스트케이스를 통과하지 못했습니다.", nullable = true)
        String errorMessage,

        @Schema(description = "현재 제출 시도 번호", example = "1")
        Integer attemptNo,

        @Schema(description = "남은 제출 가능 횟수", example = "2")
        Integer remainingAttemptCount,

        @Schema(description = "다시 제출할 수 있는지 여부", example = "true")
        Boolean canRetry,

        @Schema(description = "정답 시 다음으로 풀 수 있는 문제 ID. 다음 문제가 없으면 null", example = "3002", nullable = true)
        Long nextProblemId,

        @Schema(description = "문제 세트 완료 여부", example = "false")
        Boolean isProblemSetCompleted,

        @Schema(description = "문제 해설. 정답 또는 문제 세트 완료 시 표시할 수 있습니다.", example = "df.shape는 (행 개수, 열 개수) 튜플을 반환합니다.", nullable = true)
        String explanation
) {

    public SubmissionResponse(SubmissionView result) {
        this(
                result.submissionId(),
                result.problemId(),
                result.correct(),
                result.passedTestCount(),
                result.totalTestCount(),
                result.executionStatus(),
                result.errorMessage(),
                result.attemptNo(),
                result.remainingAttemptCount(),
                result.canRetry(),
                result.nextProblemId(),
                result.problemSetCompleted(),
                result.explanation()
        );
    }
}
