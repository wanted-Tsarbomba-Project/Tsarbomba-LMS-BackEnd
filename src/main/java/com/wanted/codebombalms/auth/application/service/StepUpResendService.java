package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.usecase.StepUpResendUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.model.StepUpChallenge;
import com.wanted.codebombalms.auth.domain.repository.LockTokenRepository;
import com.wanted.codebombalms.auth.domain.repository.StepUpTokenRepository;
import com.wanted.codebombalms.auth.domain.service.EmailSender;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StepUpResendService implements StepUpResendUseCase {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final StepUpTokenRepository stepUpTokenRepository;
    private final LockTokenRepository lockTokenRepository;
    private final UserRepository userRepository;
    private final EmailSender emailSender;

    @Value("${app.lock-url:http://localhost:8080/api/v1/auth/lock}")
    private String lockUrlBase;

    @Override
    public void resend(String stepUpToken) {
        // 1. 챌린지 토큰 검증
        if (stepUpToken == null || stepUpToken.isBlank()) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_STEP_UP_TOKEN_INVALID);
        }
        StepUpChallenge challenge = stepUpTokenRepository.find(stepUpToken)
                .orElseThrow(() -> new UnauthorizedException(AuthErrorCode.AUTH_STEP_UP_TOKEN_INVALID));

        User user = userRepository.findByUserId(challenge.userId())
                .orElseThrow(() -> new UnauthorizedException(AuthErrorCode.AUTH_STEP_UP_TOKEN_INVALID));

        // 2. 새 OTP 생성 → 덮어쓰기(TTL 5분 갱신)
        String newCode = generateCode();
        stepUpTokenRepository.save(stepUpToken,
                new StepUpChallenge(challenge.userId(), challenge.deviceFp(), challenge.country(), newCode));

        // 3. 새 잠금 토큰 발급 + 링크 조립 (의심 로그인 알림과 동일하게)
        String lockToken = UUID.randomUUID().toString();
        lockTokenRepository.save(lockToken, user.getUserId());
        String lockUrl = lockUrlBase + "?token=" + lockToken;

        // 4. 재발송 (OTP + 잠금 링크)
        emailSender.sendStepUpCode(user.getEmail(), newCode, lockUrl);
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}