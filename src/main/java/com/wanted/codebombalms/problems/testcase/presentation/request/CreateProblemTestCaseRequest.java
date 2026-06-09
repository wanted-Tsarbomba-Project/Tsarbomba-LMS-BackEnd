package com.wanted.codebombalms.problems.testcase.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProblemTestCaseRequest(
        @NotBlank(message = "테스트 코드는 필수입니다.")
        @Schema(description = "사용자 코드 실행 후 함께 실행할 채점 검증 코드", example = "assert result == df.shape", requiredMode = Schema.RequiredMode.REQUIRED)
        String testCode,

        @NotNull(message = "테스트 순서는 필수입니다.")
        @Min(value = 1, message = "테스트 순서는 1 이상이어야 합니다.")
        @Schema(description = "테스트 케이스 실행 순서", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer testOrder,

        @NotNull(message = "숨김 여부는 필수입니다.")
        @Schema(description = "학생에게 상세 실행 정보를 숨길지 여부", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean isHidden,

        @NotNull(message = "실행 제한 시간은 필수입니다.")
        @Min(value = 1, message = "실행 제한 시간은 1ms 이상이어야 합니다.")
        @Schema(description = "테스트 케이스 실행 제한 시간(ms)", example = "3000", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer timeoutMs
) {
}
