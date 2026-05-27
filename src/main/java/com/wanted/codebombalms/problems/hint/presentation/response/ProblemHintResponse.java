package com.wanted.codebombalms.problems.hint.presentation.response;

import com.wanted.codebombalms.problems.hint.application.usecase.FindProblemHintsUseCase.ProblemHintView;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProblemHintResponse(
        @Schema(description = "힌트 ID", example = "3001")
        Long hintId,

        @Schema(description = "힌트 표시 순서", example = "1")
        Integer hintOrder,

        @Schema(description = "힌트 내용", example = "DataFrame의 shape 속성을 사용해보세요.")
        String hintContent
) {
    public ProblemHintResponse(ProblemHintView result) {
        this(
                result.hintId(),
                result.hintOrder(),
                result.hintContent()
        );
    }
}
