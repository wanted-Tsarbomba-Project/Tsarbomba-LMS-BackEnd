package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.user.application.usecase.VerifyPasswordUseCase;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.ProfileEditVerificationRepository;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VerifyPasswordService implements VerifyPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileEditVerificationRepository profileEditVerificationRepository;

    @Override
    public void verify(Long userId, String rawPassword) {
        // 1. 본인 조회 (없으면 USER_NOT_FOUND)
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 2. 비밀번호 재확인 (불일치 시 400 AUT-013)
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new ValidationException(AuthErrorCode.AUTH_PASSWORD_MISMATCH);
        }

        // 3. 재인증 도장 찍기 (TTL 10분 — 이후 정보수정/비번변경 허용)
        profileEditVerificationRepository.markVerified(userId);
    }
}
