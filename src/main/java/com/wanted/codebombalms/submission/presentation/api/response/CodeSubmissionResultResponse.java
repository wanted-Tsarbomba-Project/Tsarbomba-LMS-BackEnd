package com.wanted.codebombalms.submission.presentation.api.response;

import com.wanted.codebombalms.submission.application.usecase.CodeSubmissionResultQueryUseCase.CodeSubmissionResultView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record CodeSubmissionResultResponse(
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

        @Schema(description = "채점 실패 또는 오답 메시지. 성공한 경우 null", example = "2개의 테스트케이스를 통과하지 못했습니다.", nullable = true)
        String errorMessage,

        @Schema(description = "제출 시각", example = "2026-05-27T01:47:19")
        LocalDateTime submittedAt,

        @Schema(description = "테스트케이스별 채점 결과 목록. 히든 테스트케이스는 상세 결과가 null로 반환됩니다.")
        List<TestCaseResultResponse> testCaseResults
) {

    public CodeSubmissionResultResponse(CodeSubmissionResultView result) {
        this(
                result.submissionId(),
                result.problemId(),
                result.correct(),
                result.passedTestCount(),
                result.totalTestCount(),
                result.executionStatus(),
                result.errorMessage(),
                result.submittedAt(),
                result.testCaseResults()
                        .stream()
                        .map(TestCaseResultResponse::new)
                        .toList()
        );
    }
}
