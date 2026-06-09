package com.wanted.codebombalms.problems.set.application.command;

import com.wanted.codebombalms.problems.set.domain.model.ProblemTestCaseModification;
import java.util.List;

public record ProblemUpdateCommand(
        Long problemId,
        String title,
        String content,
        Integer point,
        String startCode,
        Long hintId,
        String hint,
        String explanation,
        List<ProblemTestCaseModification> testCases
) {
}
