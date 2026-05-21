package com.wanted.codebombalms.problems.set.application.command;

public record DeleteProblemSetCommand(
        Long problemSetId,
        boolean force
) {
}
