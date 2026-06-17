package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.usecase.VerifyPasswordResetCodeUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.repository.PasswordResetRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.TooManyRequestsException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerifyPasswordResetCodeService implements VerifyPasswordResetCodeUseCase {

    private final PasswordResetRepository passwordResetRepository;

    private static final int MAX_FAIL_ATTEMPTS = 5;   // email당 10분 내 최대 실패 횟수

    @Override
    public void verifyResetCode(String email, String code) {

        // 0. 시도 횟수 제한 (email 단위) — 무차별 대입 차단 (429)
        if (passwordResetRepository.getFailCount(email) >= MAX_FAIL_ATTEMPTS) {
            throw new TooManyRequestsException(AuthErrorCode.AUTH_PASSWORD_RESET_TOO_MANY);
        }

        // 1. 코드로 저장된 이메일 비파괴 조회
        String storedEmail = passwordResetRepository.findEmailByCode(code).orElse(null);

        // 2. 코드 없음(만료/오입력) or email 불일치 → 실패 누적 후 거부
        if (storedEmail == null || !storedEmail.equals(email)) {
            passwordResetRepository.incrementFailCount(email);
            throw new ValidationException(AuthErrorCode.AUTH_PASSWORD_RESET_CODE_INVALID);
        }

        // 3. 검증 성공 — 코드는 유지(reset 단계 재사용), 실패 카운터만 리셋
        passwordResetRepository.clearFailCount(email);
    }
}
