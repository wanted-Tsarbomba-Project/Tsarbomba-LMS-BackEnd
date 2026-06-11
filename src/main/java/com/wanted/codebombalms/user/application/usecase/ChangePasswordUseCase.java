package com.wanted.codebombalms.user.application.usecase;

public interface ChangePasswordUseCase {

    void changePassword(Long userId, String newPassword, String confirmPassword);
}
