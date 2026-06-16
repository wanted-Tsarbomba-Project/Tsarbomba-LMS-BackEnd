package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.usecase.SendVerificationEmailUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.repository.EmailVerificationRepository;
import com.wanted.codebombalms.auth.domain.service.EmailSender;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.TooManyRequestsException;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class SendVerificationEmailService implements SendVerificationEmailUseCase {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailSender emailSender;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_SEND_COUNT_PER_10MIN = 5;

    @Override
    public void sendVerificationCode(String email) {

        // 1. 이미 가입된 이메일이면 차단
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException(UserErrorCode.USER_EMAIL_DUPLICATED);
        }

        // 2. 재발송 쿨다운 (1분 이내 재요청 차단)
        if (emailVerificationRepository.isRecentlySent(email)) {
            throw new TooManyRequestsException(AuthErrorCode.AUTH_EMAIL_SEND_TOO_MANY);
        }

        // 3. 발송 횟수 제한 (10분 내 최대 5회)
        long count = emailVerificationRepository.incrementSendCount(email);
        if (count > MAX_SEND_COUNT_PER_10MIN) {
            throw new TooManyRequestsException(AuthErrorCode.AUTH_EMAIL_SEND_TOO_MANY);
        }

        // 4. 6자리 인증 코드 생성
        String code = generateSixDigitCode();

        // 5. Redis 에 저장 (TTL 3분 자동)
        emailVerificationRepository.saveCode(email, code);

        // 6. 재발송 쿨다운 마킹 (TTL 1분 자동)
        emailVerificationRepository.markRecentlySent(email);

        // 7. 이메일 발송
        emailSender.sendVerificationCode(email, code);
    }

    /** 000000 ~ 999999 (6자리, 0 패딩 포함) */
    private String generateSixDigitCode() {
        int number = RANDOM.nextInt(1_000_000);
        return String.format("%06d", number);
    }
}
