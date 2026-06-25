package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.usecase.LockAccountUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.repository.LockTokenRepository;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LockAccountService implements LockAccountUseCase {

    private final LockTokenRepository lockTokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void lock(String token) {
        // 1. 토큰 검증 (GETDEL — 단일 사용)
        if (token == null || token.isBlank()) {
            throw new ValidationException(AuthErrorCode.AUTH_LOCK_TOKEN_INVALID);
        }
        Long userId = lockTokenRepository.findUserIdAndDelete(token)
                .orElseThrow(() -> new ValidationException(AuthErrorCode.AUTH_LOCK_TOKEN_INVALID));

        // 2. 계정 잠금
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ValidationException(AuthErrorCode.AUTH_LOCK_TOKEN_INVALID));
        user.lock();
        userRepository.save(user);

        // 3. 강제 로그아웃 (RT 전체 삭제)
        refreshTokenRepository.deleteByUserId(userId);
    }
}
