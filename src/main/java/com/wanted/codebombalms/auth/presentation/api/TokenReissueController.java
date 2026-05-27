package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.dto.TokenPair;
import com.wanted.codebombalms.auth.application.usecase.TokenReissueUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth - 인증", description = "회원가입 / 로그인 / 토큰 관리 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class TokenReissueController {

    private final TokenReissueUseCase tokenReissueUseCase;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(
            summary = "토큰 재발급 (RTR)",
            description = "refreshToken 쿠키로 새 accessToken/refreshToken 쌍 발급. 기존 Refresh Token 즉시 폐기 (1회용)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재발급 성공 — 새 쿠키 2개")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-004 유효하지 않은 Refresh Token / AUT-005 만료된 Refresh Token")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<Void>> reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 1. 쿠키에서 refreshToken 추출
        String refreshToken = extractCookie(request, "refreshToken")
                .orElseThrow(() -> new UnauthorizedException(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID));

        // 2. 재발급 수행
        TokenPair pair = tokenReissueUseCase.reissue(refreshToken);

        // 3. 새 쿠키 2개 발급 (기존 쿠키 덮어쓰기)
        response.addCookie(createCookie(
                "accessToken",
                pair.accessToken(),
                (int) (jwtTokenProvider.getAccessExpiration() / 1000)
        ));
        response.addCookie(createCookie(
                "refreshToken",
                pair.refreshToken(),
                (int) (jwtTokenProvider.getRefreshExpiration() / 1000)
        ));

        return ResponseEntity.ok(ApiResponse.success(
                AuthResponseCode.TOKEN_REISSUED,
                AuthResponseMessage.TOKEN_REISSUED
        ));
    }

    private java.util.Optional<String> extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return java.util.Optional.empty();
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                return java.util.Optional.of(cookie.getValue());
            }
        }
        return java.util.Optional.empty();
    }

    private Cookie createCookie(String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}