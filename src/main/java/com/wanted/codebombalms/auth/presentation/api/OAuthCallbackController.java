package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.dto.GoogleCallbackResult;
import com.wanted.codebombalms.auth.application.usecase.GoogleCallbackUseCase;
import com.wanted.codebombalms.auth.infrastructure.oauth.OAuthProperties;
import com.wanted.codebombalms.auth.presentation.api.support.AuthCookieFactory;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(name = "Auth - 소셜 로그인", description = "구글 OAuth2 로그인 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/auth/oauth2")
@RequiredArgsConstructor
public class OAuthCallbackController {

    private final GoogleCallbackUseCase googleCallbackUseCase;
    private final AuthCookieFactory authCookieFactory;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthProperties oAuthProperties;

    private static final int TEMP_TOKEN_MAX_AGE = 600; // 10분

    @Operation(
            summary = "구글 콜백 처리",
            description = "구글이 redirect 하는 엔드포인트. 기존 회원 → 토큰 쿠키 후 메인, 신규 회원 → TEMP_TOKEN 쿠키 후 추가정보 페이지."
    )
    @GetMapping("/callback/google")
    public ResponseEntity<Void> googleCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletResponse response
    ) {
        GoogleCallbackResult result = googleCallbackUseCase.handleCallback(code, state);

        if (result.newUser()) {
            // 신규 회원 → TEMP_TOKEN 쿠키 + 추가정보 페이지로
            response.addCookie(authCookieFactory.create(
                    "tempToken", result.tempToken(), TEMP_TOKEN_MAX_AGE));
            return redirect(oAuthProperties.getAdditionalInfoUri());
        }

        // 기존 회원 → AT/RT 쿠키 + 메인으로
        response.addCookie(authCookieFactory.create(
                "accessToken", result.accessToken(),
                (int) (jwtTokenProvider.getAccessExpiration() / 1000)));
        response.addCookie(authCookieFactory.create(
                "refreshToken", result.refreshToken(),
                (int) (jwtTokenProvider.getRefreshExpiration() / 1000)));
        return redirect(oAuthProperties.getSuccessUri());
    }

    private ResponseEntity<Void> redirect(String uri) {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(uri)).build();
    }
}
