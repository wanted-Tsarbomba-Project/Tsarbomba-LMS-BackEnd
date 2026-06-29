package com.wanted.codebombalms.auth.application.command;

public record StepUpVerifyCommand(
        String stepUpToken,
        String code,
        boolean trustDevice
) {
}
