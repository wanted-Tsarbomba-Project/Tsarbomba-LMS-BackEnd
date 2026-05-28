package com.wanted.codebombalms.problems.testcase.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateProblemTestCaseRequest(
        @Schema(
                description = "채점용 검증 코드. 사용자 코드 실행 후 함께 실행됩니다.",
                example = "assert result == df.shape",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String testCode,

        @Schema(
                description = "운영자가 참고할 기대 결과. 실제 채점은 testCode 실행 결과를 기준으로 합니다.",
                example = "df.shape",
                nullable = true
        )
        String expectedResult,

        @Schema(
                description = "테스트케이스 실행 순서",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Integer testOrder,

        @Schema(
                description = "히든 테스트케이스 여부. true이면 학생에게 테스트 코드, 실제 출력, 오류 메시지, 실행 시간 같은 상세 정보를 노출하지 않습니다.",
                example = "false",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Boolean isHidden,

        @Schema(
                description = "테스트케이스 실행 제한 시간(ms). 기본값은 3000ms입니다.",
                example = "3000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Integer timeoutMs
) {
}
