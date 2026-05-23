package com.wanted.codebombalms.problems.set.application.command;

import java.util.List;

public record RegisterProblemSetCommand(
        String title,
        String categoryName,
        String difficulty,
        String description,
        String dataFileName,
        List<ProblemCreateCommand> problems
) {
}
