package com.wanted.codebombalms.domain.auth.controller;

import com.wanted.codebombalms.domain.auth.dto.TokenPair;
import com.wanted.codebombalms.domain.auth.dto.request.LoginRequest;
import com.wanted.codebombalms.domain.auth.dto.request.SignupRequest;
import com.wanted.codebombalms.domain.auth.service.AuthService;
import com.wanted.codebombalms.global.common.ResponseDTO;
import com.wanted.codebombalms.global.jwt.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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
}