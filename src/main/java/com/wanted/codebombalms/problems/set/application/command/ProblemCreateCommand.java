package com.wanted.codebombalms.problems.set.application.command;

import com.wanted.codebombalms.problems.set.domain.model.ProblemTestCaseRegistration;
import java.util.List;

public record ProblemCreateCommand(
        String title,
        String content,
        Integer point,
        String startCode,
        String hint,
        String explanation,
        List<ProblemTestCaseRegistration> testCases
) {
}
