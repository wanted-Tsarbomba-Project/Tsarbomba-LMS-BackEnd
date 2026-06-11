package com.wanted.codebombalms.user.application.usecase;

public interface VerifyPasswordUseCase {

    void verify(Long userId, String rawPassword);
}
