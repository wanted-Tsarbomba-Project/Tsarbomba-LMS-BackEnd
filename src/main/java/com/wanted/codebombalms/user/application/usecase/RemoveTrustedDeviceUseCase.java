package com.wanted.codebombalms.user.application.usecase;

public interface RemoveTrustedDeviceUseCase {

    void remove(Long userId, Long trustedDeviceId);
}
