package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.usecase.SendPasswordResetCodeUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.repository.PasswordResetRepository;
import com.wanted.codebombalms.auth.domain.service.EmailSender;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.TooManyRequestsException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.AuthProvider;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class SendPasswordResetCodeService implements SendPasswordResetCodeUseCase {

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final EmailSender emailSender;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_SEND_COUNT_PER_10MIN = 5;

    @Override
    public void sendResetCode(String email) {

        // 1. 가입된 회원인지 확인 (없으면 404)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 2. 소셜 가입 계정은 비밀번호 재설정 불가 (400)
        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new ValidationException(UserErrorCode.USER_SOCIAL_ACCOUNT_NO_PASSWORD);
        }

        // 3. 재발송 쿨다운 (1분 이내 재요청 차단)
        if (passwordResetRepository.isRecentlySent(email)) {
            throw new TooManyRequestsException(AuthErrorCode.AUTH_EMAIL_SEND_TOO_MANY);
        }

        // 4. 발송 횟수 제한 (10분 내 최대 5회)
        long count = passwordResetRepository.incrementSendCount(email);
        if (count > MAX_SEND_COUNT_PER_10MIN) {
            throw new TooManyRequestsException(AuthErrorCode.AUTH_EMAIL_SEND_TOO_MANY);
        }

        // 5. 6자리 재설정 코드 생성
        String code = generateSixDigitCode();

        // 6. Redis 저장 (code → email, TTL 10분)
        passwordResetRepository.saveCode(email, code);

        // 7. 재발송 쿨다운 마킹 (TTL 1분)
        passwordResetRepository.markRecentlySent(email);

        // 8. 재설정 코드 이메일 발송
        emailSender.sendPasswordResetCode(email, code);
    }

    /** 000000 ~ 999999 (6자리, 0 패딩 포함) */
    private String generateSixDigitCode() {
        int number = RANDOM.nextInt(1_000_000);
        return String.format("%06d", number);
    }
}
