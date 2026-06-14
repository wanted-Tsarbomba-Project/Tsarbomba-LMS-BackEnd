package com.wanted.codebombalms.auth.application.usecase;

public interface ResetPasswordUseCase {

    void resetPassword(String code, String newPassword);
}
