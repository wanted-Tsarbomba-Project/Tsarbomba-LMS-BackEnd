package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.usecase.VerifyPasswordResetCodeUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.repository.PasswordResetRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerifyPasswordResetCodeService implements VerifyPasswordResetCodeUseCase {

    private final PasswordResetRepository passwordResetRepository;

    @Override
    public void verifyResetCode(String email, String code) {

        // 1. 코드로 저장된 이메일 역조회
        Optional<String> storedEmail = passwordResetRepository.findEmailByCode(code);

        // 2. 코드가 없으면 만료된 것 (Redis TTL 경과로 자동 삭제됨)
        if (storedEmail.isEmpty()) {
            throw new ValidationException(AuthErrorCode.AUTH_PASSWORD_RESET_CODE_EXPIRED);
        }

        // 3. 코드는 살아있지만 요청 이메일과 불일치 → 유효하지 않은 코드
        if (!storedEmail.get().equals(email)) {
            throw new ValidationException(AuthErrorCode.AUTH_PASSWORD_RESET_CODE_INVALID);
        }

        // 4. 검증 성공 — 코드는 삭제하지 않음 (reset 단계에서 재사용)
    }
}
