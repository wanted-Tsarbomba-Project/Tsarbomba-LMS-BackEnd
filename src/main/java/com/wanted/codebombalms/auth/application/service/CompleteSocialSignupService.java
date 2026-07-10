package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.dto.TokenPair;
import com.wanted.codebombalms.auth.application.usecase.CompleteSocialSignupUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.model.OAuthTempData;
import com.wanted.codebombalms.auth.domain.model.RefreshToken;
import com.wanted.codebombalms.auth.domain.repository.RefreshTokenRepository;
import com.wanted.codebombalms.auth.domain.repository.TempTokenRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.AuthProvider;
import com.wanted.codebombalms.user.domain.model.TermsType;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserAgreement;
import com.wanted.codebombalms.user.domain.repository.UserAgreementRepository;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CompleteSocialSignupService implements CompleteSocialSignupUseCase {

    private final TempTokenRepository tempTokenRepository;
    private final UserRepository userRepository;
    private final UserAgreementRepository userAgreementRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthSessionManager authSessionManager;

    @Override
    public TokenPair complete(String tempToken, String nickname, String phone,
                              boolean termsOfServiceAgreed, boolean privacyPolicyAgreed) {

        // 1. TEMP_TOKEN 조회 (삭제 X — 검증 실패 시 재시도 가능하게). 없으면 401
        OAuthTempData data = tempTokenRepository.find(tempToken)
                .orElseThrow(() -> new UnauthorizedException(AuthErrorCode.AUTH_TEMP_TOKEN_INVALID));

        // 2. 필수 약관 동의 확인 (이용약관 + 개인정보 수집·이용)
        if (!termsOfServiceAgreed || !privacyPolicyAgreed) {
            throw new ValidationException(UserErrorCode.USER_TERMS_NOT_AGREED);
        }

        // 3. 닉네임 중복 검사 (USR-003)
        if (userRepository.existsByNickname(nickname)) {
            throw new ValidationException(UserErrorCode.USER_NICKNAME_DUPLICATED);
        }

        // 4. 이메일 중복 방어 (중복 제출 등 — USR-002)
        if (userRepository.existsByEmail(data.email())) {
            throw new ValidationException(UserErrorCode.USER_EMAIL_DUPLICATED);
        }

        // 5. 소셜 회원 생성 (GOOGLE, STUDENT)
        User saved = userRepository.save(
                User.createSocialUser(data.email(), data.name(), nickname, phone, AuthProvider.GOOGLE)
        );

        // 6. 약관 동의 이력 저장 (SERVICE·PRIVACY 2건 — 버전은 서버가 스탬프)
        userAgreementRepository.saveAll(List.of(
                UserAgreement.agree(saved.getUserId(), TermsType.SERVICE),
                UserAgreement.agree(saved.getUserId(), TermsType.PRIVACY)
        ));

        // 7. 가입 성공 후 TEMP_TOKEN 소비 (단일 사용 — 이 시점에 삭제)
        tempTokenRepository.findAndDelete(tempToken);

        // 8. 토큰 발급 (가입 직후 바로 로그인) — P0 세션 하드닝: sid 발급 후 AT 에 바인딩
        String sid = authSessionManager.open(saved.getUserId());
        String accessToken = jwtTokenProvider.generateAccessToken(
                saved.getUserId(), saved.getNickname(), saved.getRole(), sid);
        String refreshToken = jwtTokenProvider.generateRefreshToken(saved.getUserId());

        refreshTokenRepository.save(
                RefreshToken.issue(saved.getUserId(), refreshToken, jwtTokenProvider.getRefreshExpiration())
        );

        return new TokenPair(accessToken, refreshToken);
    }
}