package com.wanted.codebombalms.problems.set.application.command;

public record ProblemUpdateCommand(
        Long problemId,
        String title,
        String content,
        Integer point,
        String startCode,
        String answer,
        Long hintId,
        String hint,
        String explanation
) {
}
