package com.wanted.codebombalms.problems.set.application.command;

import java.util.List;

public record UpdateProblemSetCommand(
        Long problemSetId,
        String title,
        String categoryName,
        String difficulty,
        String description,
        String dataFileName,
        List<ProblemUpdateCommand> problems
) {
}
