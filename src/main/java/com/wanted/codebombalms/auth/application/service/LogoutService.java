package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.usecase.LogoutUseCase;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}