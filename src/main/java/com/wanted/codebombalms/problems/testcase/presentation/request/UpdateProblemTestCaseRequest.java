package com.wanted.codebombalms.problems.testcase.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateProblemTestCaseRequest(
        @NotBlank(message = "테스트 코드는 필수입니다.")
        String testCode,

        @NotNull(message = "테스트 순서는 필수입니다.")
        @Min(value = 1, message = "테스트 순서는 1 이상이어야 합니다.")
        @Schema(description = "테스트 케이스 실행 순서", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer testOrder,

        @NotNull(message = "숨김 여부는 필수입니다.")
        Boolean isHidden,
        @NotNull(message = "실행 제한 시간은 필수입니다.")
        @Min(value = 100, message = "실행 제한 시간은 100ms 이상이어야 합니다.")
        @Max(value = 10000, message = "실행 제한 시간은 10000ms 이하여야 합니다.")
        Integer timeoutMs
) {
}
