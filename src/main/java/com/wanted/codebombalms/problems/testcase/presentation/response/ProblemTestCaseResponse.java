package com.wanted.codebombalms.problems.testcase.presentation.response;

import com.wanted.codebombalms.problems.testcase.application.usecase.ProblemTestCaseCommandUseCase;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemTestCaseResponse(
        @Schema(description = "테스트 케이스 ID", example = "3001")
        Long testCaseId,
        @Schema(description = "연결된 문제 ID", example = "3001")
        Long problemId,
        @Schema(description = "채점 검증 코드", example = "assert result == df.shape")
        String testCode,
        @Schema(description = "실행 순서", example = "1")
        Integer testOrder,
        @Schema(description = "학생에게 상세 실행 정보를 숨길지 여부", example = "false")
        Boolean isHidden,
        @Schema(description = "실행 제한 시간(ms)", example = "3000")
        Integer timeoutMs,
        @Schema(description = "테스트 케이스 상태", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
        String status
) {
    public static ProblemTestCaseResponse from(ProblemTestCaseCommandUseCase.TestCaseView view) {
        return new ProblemTestCaseResponse(
                view.testCaseId(),
                view.problemId(),
                view.testCode(),
                view.testOrder(),
                view.hidden(),
                view.timeoutMs(),
                view.status()
        );
    }
}
