package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.usecase.ResetPasswordUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.repository.PasswordResetRepository;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.TooManyRequestsException;
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

    private static final int MAX_FAIL_ATTEMPTS = 5;   // email당 10분 내 최대 실패 횟수

    @Override
    public void resetPassword(String email, String code, String newPassword) {

        // 0. 시도 횟수 제한 (email 단위) — 무차별 대입 차단 (429)
        if (passwordResetRepository.getFailCount(email) >= MAX_FAIL_ATTEMPTS) {
            throw new TooManyRequestsException(AuthErrorCode.AUTH_PASSWORD_RESET_TOO_MANY);
        }

        // 1. 코드 비파괴 조회 (짝 안 맞으면 코드 보존 → 타인 코드 무효화 DoS 방지)
        String storedEmail = passwordResetRepository.findEmailByCode(code).orElse(null);

        // 2. 코드 없음(만료/오입력) or email 불일치 → 실패 누적 후 거부
        if (storedEmail == null || !storedEmail.equals(email)) {
            passwordResetRepository.incrementFailCount(email);
            throw new ValidationException(AuthErrorCode.AUTH_PASSWORD_RESET_CODE_INVALID);
        }

        // 3. 짝 일치 — 원자적 소비 (동시 reset 중복 사용 차단)
        if (passwordResetRepository.findAndDeleteByCode(code).isEmpty()) {
            throw new ValidationException(AuthErrorCode.AUTH_PASSWORD_RESET_CODE_EXPIRED);
        }

        // 4. 사용자 조회 (코드 발급 후 탈퇴 등 엣지 — @SQLRestriction으로 탈퇴 계정은 자동 차단)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 5. 새 비밀번호 인코딩 후 교체
        user.changePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 6. Refresh Token 전체 삭제 (강제 재로그인)
        refreshTokenRepository.deleteByUserId(user.getUserId());

        // 7. 성공 — 실패 카운터 초기화
        passwordResetRepository.clearFailCount(email);

        log.info("비밀번호 재설정 완료 - userId={}", user.getUserId());
    }
}
