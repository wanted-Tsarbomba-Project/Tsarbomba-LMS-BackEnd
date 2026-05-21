package com.wanted.codebombalms.problems.progress.application.query;

public record GetProblemProgressQuery(
        Long problemSetId,
        Long userId
) {
}
