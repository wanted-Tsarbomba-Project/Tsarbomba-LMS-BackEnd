package com.wanted.codebombalms.problems.set.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProblemTestCaseCreateItemRequest(
        @NotBlank(message = "테스트 코드는 필수입니다.")
        @Schema(description = "실제 채점에 사용하는 검증 코드", example = "assert result == df.shape")
        String testCode,

        @NotNull(message = "숨김 여부는 필수입니다.")
        @Schema(description = "학생에게 테스트 상세 결과를 숨길지 여부", example = "true")
        Boolean isHidden,

        @Min(value = 100, message = "실행 제한 시간은 100ms 이상이어야 합니다.")
        @Max(value = 10000, message = "실행 제한 시간은 10000ms 이하여야 합니다.")
        @Schema(description = "실행 제한 시간(ms). 생략 시 3000ms", example = "3000", nullable = true)
        Integer timeoutMs
) {
}
