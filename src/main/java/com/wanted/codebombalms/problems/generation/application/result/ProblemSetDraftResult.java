package com.wanted.codebombalms.problems.generation.application.result;

import com.wanted.codebombalms.problems.generation.domain.ProblemSetDraft;
import java.util.List;

public record ProblemSetDraftResult(
        String answer,
        ProblemSetDraft draft,
        List<String> usedTools
) {
}
