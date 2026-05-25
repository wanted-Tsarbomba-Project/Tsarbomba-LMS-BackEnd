package com.wanted.codebombalms.auth.application.usecase;

public interface VerifyEmailCodeUseCase {
    
    void verifyCode(String email, String code);
}