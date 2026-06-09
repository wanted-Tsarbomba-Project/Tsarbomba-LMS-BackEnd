package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.user.application.usecase.WithdrawUserUseCase;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WithdrawUserService implements WithdrawUserUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void withdraw(Long userId) {
        // 1. 본인 조회 (없으면 USER_NOT_FOUND)
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 2. Soft Delete (deleted_at 기록)
        user.softDelete();
        userRepository.save(user);

        // 3. Refresh Token 전체 삭제 (강제 로그아웃)
        refreshTokenRepository.deleteByUserId(userId);
    }
}
