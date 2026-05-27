package com.wanted.codebombalms.problems.testcase.presentation.api.response;

import com.wanted.codebombalms.problems.testcase.application.usecase.ProblemTestCaseCommandUseCase;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemTestCaseResponse(
        @Schema(description = "테스트케이스 ID", example = "3001")
        Long testCaseId,

        @Schema(description = "테스트케이스가 연결된 문제 ID", example = "3001")
        Long problemId,

        @Schema(
                description = "채점용 검증 코드. 사용자 코드 실행 후 함께 실행됩니다.",
                example = "assert result == df.shape"
        )
        String testCode,

        @Schema(
                description = "운영자가 참고할 기대 결과. 실제 채점은 testCode 실행 결과를 기준으로 합니다.",
                example = "df.shape",
                nullable = true
        )
        String expectedResult,

        @Schema(description = "테스트케이스 실행 순서", example = "1")
        Integer testOrder,

        @Schema(
                description = "히든 테스트케이스 여부. true이면 학생 결과 화면에서 actualOutput, errorMessage, executionTimeMs 같은 상세 실행 정보를 숨깁니다.",
                example = "false"
        )
        Boolean isHidden,

        @Schema(description = "테스트케이스 실행 제한 시간(ms)", example = "3000")
        Integer timeoutMs,

        @Schema(description = "테스트케이스 상태", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
        String status
) {

    public static ProblemTestCaseResponse from(ProblemTestCaseCommandUseCase.TestCaseView view) {
        return new ProblemTestCaseResponse(
                view.testCaseId(),
                view.problemId(),
                view.testCode(),
                view.expectedResult(),
                view.testOrder(),
                view.hidden(),
                view.timeoutMs(),
                view.status()
        );
    }
}
