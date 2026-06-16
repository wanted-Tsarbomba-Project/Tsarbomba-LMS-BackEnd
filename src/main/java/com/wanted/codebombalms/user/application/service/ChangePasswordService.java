package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.user.application.usecase.ChangePasswordUseCase;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.ProfileEditVerificationRepository;
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
public class ChangePasswordService implements ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileEditVerificationRepository profileEditVerificationRepository;

    @Override
    public void changePassword(Long userId, String newPassword, String confirmPassword) {
        // 0. 재인증 게이트 — verify-password 통과 도장 없으면 거부 (403 USR-011)
        if (!profileEditVerificationRepository.isVerified(userId)) {
            throw new ForbiddenException(UserErrorCode.USER_REVERIFICATION_REQUIRED);
        }

        // 1. 본인 조회 (없으면 USER_NOT_FOUND)
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 2. 새 비밀번호 == 확인값 (불일치 시 400 USR-006)
        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException(UserErrorCode.USER_PASSWORD_CONFIRM_MISMATCH);
        }

        // 3. 인코딩 후 교체
        user.changePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 4. Refresh Token 전체 삭제 (강제 재로그인)
        refreshTokenRepository.deleteByUserId(userId);

        // 5. 재인증 도장 제거 (어차피 강제 재로그인 → 세션 종료)
        profileEditVerificationRepository.clearVerified(userId);

        log.info("비밀번호 변경 완료 - userId={}", userId);
    }
}
