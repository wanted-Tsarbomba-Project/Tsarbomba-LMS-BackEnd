package com.wanted.codebombalms.auth.application.usecase;

public interface VerifyPasswordResetCodeUseCase {

    void verifyResetCode(String email, String code);
}
