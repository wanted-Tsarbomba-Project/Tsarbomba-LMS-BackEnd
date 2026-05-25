package com.wanted.codebombalms.auth.application.usecase;

public interface SendVerificationEmailUseCase {

    void sendVerificationCode(String email);
}