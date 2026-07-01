package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.user.application.usecase.WithdrawUserUseCase;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WithdrawUserService implements WithdrawUserUseCase {

    // 소셜 계정 탈퇴 확인 문구 (비밀번호 대체)
    private static final String WITHDRAW_CONFIRM_PHRASE = "탈퇴하겠습니다";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void withdraw(Long userId, String rawPassword, String confirmText) {
        // 1. 본인 조회 (없으면 USER_NOT_FOUND)
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 2. 본인 재확인 — 계정 종류별 분기
        if (user.isSocialAccount()) {
            // 소셜 계정: 비밀번호가 없으므로 확인 문구로 대체 (불일치 시 400 USR-013)
            if (!WITHDRAW_CONFIRM_PHRASE.equals(confirmText)) {
                throw new ValidationException(UserErrorCode.USER_WITHDRAW_CONFIRM_MISMATCH);
            }
        } else {
            // LOCAL 계정: 비밀번호 재확인 (불일치 시 400 AUT-013)
            if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                throw new ValidationException(AuthErrorCode.AUTH_PASSWORD_MISMATCH);
            }
        }

        // 3. Soft Delete (deleted_at 기록)
        user.softDelete();
        userRepository.save(user);

        // 4. Refresh Token 전체 삭제 (강제 로그아웃)
        refreshTokenRepository.deleteByUserId(userId);

        // 5. 감사 추적 로깅
        log.info("회원 탈퇴 처리 완료 - userId={}, social={}, deletedAt={}",
                userId, user.isSocialAccount(), user.getDeletedAt());
    }
}
