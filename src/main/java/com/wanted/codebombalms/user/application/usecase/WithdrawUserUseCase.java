package com.wanted.codebombalms.user.application.usecase;

public interface WithdrawUserUseCase {

    void withdraw(Long userId, String rawPassword);
}
