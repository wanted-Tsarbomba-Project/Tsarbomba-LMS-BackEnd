package com.wanted.codebombalms.auth.application.usecase;

public interface SendPasswordResetCodeUseCase {

    void sendResetCode(String email);
}
