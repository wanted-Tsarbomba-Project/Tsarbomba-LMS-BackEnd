package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.command.SignupCommand;
import com.wanted.codebombalms.auth.application.port.AuthMetricsPort;
import com.wanted.codebombalms.auth.application.usecase.SignupUseCase;
import com.wanted.codebombalms.auth.domain.repository.EmailVerificationRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SignupService implements SignupUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationRepository emailVerificationRepository;
    private final AuthMetricsPort authMetrics;

    @Override
    public Long signup(SignupCommand command) {
        try {
            // 1. 이메일 인증 완료 여부 확인 (보안 — 인증 우회 차단)
            if (!emailVerificationRepository.isVerified(command.email())) {
                throw new ValidationException(UserErrorCode.USER_EMAIL_NOT_VERIFIED);
            }

            // 2. 비밀번호 / 비밀번호 확인 일치 검증
            if (!command.rawpassword().equals(command.passwordConfirm())) {
                throw new ValidationException(UserErrorCode.USER_PASSWORD_CONFIRM_MISMATCH);
            }

            // 3. 이메일 중복 체크
            if (userRepository.existsByEmail(command.email())) {
                throw new ConflictException(UserErrorCode.USER_EMAIL_DUPLICATED);
            }

            // 4. 닉네임 중복 체크
            if (userRepository.existsByNickname(command.nickname())) {
                throw new ConflictException(UserErrorCode.USER_NICKNAME_DUPLICATED);
            }

            // 5. 비밀번호 인코딩
            String encodedPassword = passwordEncoder.encode(command.rawpassword());

            // 6. 도메인 객체 생성
            User user = User.createLocalUser(
                    command.email(),
                    encodedPassword,
                    command.name(),
                    command.nickname(),
                    command.phone()
            );

            // 7. 저장
            User saved = userRepository.save(user);

            // 8. 회원가입 완료 — 인증 플래그 정리
            emailVerificationRepository.clearVerified(command.email());

            authMetrics.recordSignupSuccess();   // ← KPI: 회원가입 완료
            return saved.getUserId();
        } catch (ValidationException | ConflictException e) {
            authMetrics.recordSignupFail();       // ← KPI: 검증/중복 실패
            throw e;
        } catch (DataIntegrityViolationException e) {
            authMetrics.recordSignupFail();       // ← KPI: 동시 가입 경쟁으로 DB 유니크 위반
            throw e;
        }
    }
}
