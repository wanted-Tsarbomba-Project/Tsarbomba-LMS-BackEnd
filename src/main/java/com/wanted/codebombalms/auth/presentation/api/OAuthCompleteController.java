package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.dto.TokenPair;
import com.wanted.codebombalms.auth.application.usecase.CompleteSocialSignupUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.presentation.api.dto.request.OAuthCompleteRequest;
import com.wanted.codebombalms.auth.presentation.api.support.AuthCookieFactory;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth - 소셜 로그인", description = "구글 OAuth2 로그인 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/auth/oauth2")
@RequiredArgsConstructor
public class OAuthCompleteController {

    private final CompleteSocialSignupUseCase completeSocialSignupUseCase;
    private final AuthCookieFactory authCookieFactory;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(
            summary = "소셜 추가 정보 제출",
            description = "TEMP_TOKEN 검증 후 닉네임/전화번호로 가입 완료. 토큰 쿠키 발급 + TEMP_TOKEN 제거."
    )
    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<Void>> complete(
            @CookieValue(name = "tempToken", required = false) String tempToken,
            @Valid @RequestBody OAuthCompleteRequest request,
            HttpServletResponse response
    ) {
        // TEMP_TOKEN 쿠키 없으면 401
        if (tempToken == null || tempToken.isBlank()) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_TEMP_TOKEN_INVALID);
        }

        TokenPair tokens = completeSocialSignupUseCase.complete(
                tempToken, request.nickname(), request.phone());

        // AT/RT 쿠키 발급 + TEMP_TOKEN 제거
        response.addCookie(authCookieFactory.create(
                "accessToken", tokens.accessToken(),
                (int) (jwtTokenProvider.getAccessExpiration() / 1000)));
        response.addCookie(authCookieFactory.create(
                "refreshToken", tokens.refreshToken(),
                (int) (jwtTokenProvider.getRefreshExpiration() / 1000)));
        response.addCookie(authCookieFactory.expired("tempToken"));

        return ResponseEntity.ok(ApiResponse.success(
                AuthResponseCode.OAUTH_SIGNUP_COMPLETED,
                AuthResponseMessage.OAUTH_SIGNUP_COMPLETED,
                null
        ));
    }
}
