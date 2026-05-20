package com.wanted.codebombalms.domain.auth.controller;

import com.wanted.codebombalms.domain.auth.dto.TokenPair;
import com.wanted.codebombalms.domain.auth.dto.request.LoginRequest;
import com.wanted.codebombalms.domain.auth.dto.request.SignupRequest;
import com.wanted.codebombalms.domain.auth.service.AuthService;
import com.wanted.codebombalms.global.presentation.api.commonLegacy.ResponseDTO;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<ResponseDTO> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(HttpStatus.CREATED, "회원가입 성공", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(@Valid @RequestBody LoginRequest request) {
        TokenPair tokens = authService.login(request);

        ResponseCookie accessTokenCookie = createAccessTokenCookie(tokens.accessToken());
        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(tokens.refreshToken());

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new ResponseDTO(HttpStatus.OK, "로그인 성공", null));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<ResponseDTO> logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);

        ResponseCookie expiredAccessTokenCookie = createExpiredCookie("accessToken");
        ResponseCookie expiredRefreshTokenCookie = createExpiredCookie("refreshToken");

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, expiredRefreshTokenCookie.toString())
                .body(new ResponseDTO(HttpStatus.OK, "로그아웃 성공", null));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ResponseDTO> reissue(@CookieValue("refreshToken") String refreshToken) {
        TokenPair tokens = authService.reissue(refreshToken);

        ResponseCookie accessTokenCookie = createAccessTokenCookie(tokens.accessToken());
        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(tokens.refreshToken());

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new ResponseDTO(HttpStatus.OK, "토큰 재발급 성공", null));
    }

    // ===== 쿠키 생성 헬퍼 =====
    private ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(false)  // 로컬 개발 환경, 운영 시 true
                .path("/")
                .maxAge(jwtTokenProvider.getAccessExpiration() / 1000)
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false)  // 로컬 개발 환경, 운영 시 true
                .path("/")
                .maxAge(jwtTokenProvider.getRefreshExpiration() / 1000)
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie createExpiredCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)  // ← 즉시 만료
                .sameSite("Lax")
                .build();
    }
}