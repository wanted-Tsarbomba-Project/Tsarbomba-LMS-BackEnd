package com.wanted.codebombalms.problems.hint.presentation.api.response;

import com.wanted.codebombalms.problems.hint.application.usecase.FindProblemHintsUseCase.ProblemHintView;

public record ProblemHintResponse(
        Long hintId,
        Integer hintOrder,
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
