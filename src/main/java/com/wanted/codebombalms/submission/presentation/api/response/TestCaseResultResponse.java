package com.wanted.codebombalms.submission.presentation.api.response;

import com.wanted.codebombalms.submission.application.usecase.CodeSubmissionResultQueryUseCase.TestCaseResultView;
import io.swagger.v3.oas.annotations.media.Schema;

public record TestCaseResultResponse(
        @Schema(description = "테스트케이스 ID", example = "3001")
        Long testCaseId,

        @Schema(description = "테스트케이스 통과 여부", example = "true")
        Boolean isPassed,

        @Schema(description = "히든 테스트케이스 여부. true이면 상세 결과를 노출하지 않습니다.", example = "false")
        Boolean isHidden,

        @Schema(description = "실제 실행 출력. 히든 테스트케이스인 경우 null", example = "(100, 5)", nullable = true)
        String actualOutput,

        @Schema(description = "테스트케이스 실행 오류 메시지. 히든 테스트케이스인 경우 null", example = "AssertionError", nullable = true)
        String errorMessage,

        @Schema(description = "테스트케이스 실행 시간(ms). 히든 테스트케이스인 경우 null", example = "120", nullable = true)
        Integer executionTimeMs
) {

    public TestCaseResultResponse(TestCaseResultView result) {
        this(
                result.testCaseId(),
                result.passed(),
                result.hidden(),
                result.actualOutput(),
                result.errorMessage(),
                result.executionTimeMs()
        );
    }
}
