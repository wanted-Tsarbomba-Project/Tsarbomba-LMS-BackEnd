package com.wanted.codebombalms.auth.application.command;

public record LoginCommand(
        String email,
        String rawpassword
) {
}