package com.wanted.codebombalms.auth.application.command;

public record SignupCommand(
        String email,
        String rawpassword,
        String passwordConfirm,
        String name,
        String nickname,
        String phone
) {

}
