package com.wanted.codebombalms.problems.set.application.command;

public record ProblemCreateCommand(
        String title,
        String content,
        Integer point,
        String startCode,
        String answer,
        String hint,
        String explanation
) {
}