package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.usecase.VerifyEmailCodeUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.repository.EmailVerificationRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerifyEmailCodeService implements VerifyEmailCodeUseCase {

    private final EmailVerificationRepository emailVerificationRepository;

    @Override
    public void verifyCode(String email, String code) {

        // 1. Redis 에서 저장된 코드 조회
        Optional<String> storedCode = emailVerificationRepository.findCode(email);

        // 2. 코드가 없으면 만료된 것 (Redis TTL 경과로 자동 삭제됨)
        if (storedCode.isEmpty()) {
            throw new ValidationException(AuthErrorCode.AUTH_CODE_EXPIRED);
        }

        // 3. 코드 불일치
        if (!storedCode.get().equals(code)) {
            throw new ValidationException(AuthErrorCode.AUTH_CODE_INVALID);
        }

        // 4. 검증 성공 — 단일 사용 보장 위해 코드 즉시 삭제
        emailVerificationRepository.deleteCode(email);

        // 5. 인증 완료 플래그 저장 (TTL 30분 자동)
        emailVerificationRepository.markVerified(email);
    }
}
