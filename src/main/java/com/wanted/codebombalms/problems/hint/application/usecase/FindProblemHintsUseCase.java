package com.wanted.codebombalms.problems.hint.application.usecase;

import java.util.List;

public interface FindProblemHintsUseCase {

    List<ProblemHintView> handle(Long problemId);

    record ProblemHintView(
            Long hintId,
            Integer hintOrder,
            String hintContent
    ) {
    }
}
