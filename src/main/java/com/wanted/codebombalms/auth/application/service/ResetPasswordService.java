package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.usecase.ResetPasswordUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.repository.PasswordResetRepository;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
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
public class ResetPasswordService implements ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void resetPassword(String code, String newPassword) {

        // 1. 코드로 저장된 이메일 역조회 (없으면 만료)
        String email = passwordResetRepository.findEmailByCode(code)
                .orElseThrow(() -> new ValidationException(AuthErrorCode.AUTH_PASSWORD_RESET_CODE_EXPIRED));

        // 2. 이메일로 사용자 조회 (없으면 404 — 코드 발급 후 탈퇴 등 엣지 케이스)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 3. 새 비밀번호 인코딩 후 교체
        user.changePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 4. 재설정 코드 삭제 (단일 사용 보장)
        passwordResetRepository.deleteByCode(code);

        // 5. Refresh Token 전체 삭제 (강제 재로그인)
        refreshTokenRepository.deleteByUserId(user.getUserId());

        log.info("비밀번호 재설정 완료 - userId={}", user.getUserId());
    }
}
