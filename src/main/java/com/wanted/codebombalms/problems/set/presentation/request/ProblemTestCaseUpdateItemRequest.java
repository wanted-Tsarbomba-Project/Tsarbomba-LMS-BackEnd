package com.wanted.codebombalms.problems.set.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemTestCaseUpdateItemRequest(
        @Schema(description = "기존 테스트케이스 ID. 신규 테스트케이스는 null", example = "3001", nullable = true)
        Long testCaseId,

        @Schema(description = "실제 채점에 사용하는 검증 코드", example = "assert result == df.shape")
        String testCode,

        @Schema(description = "학생에게 테스트 상세 결과를 숨길지 여부", example = "true")
        Boolean isHidden,

        @Schema(description = "실행 제한 시간(ms). 생략 시 3000ms", example = "3000", nullable = true)
        Integer timeoutMs
) {
}
