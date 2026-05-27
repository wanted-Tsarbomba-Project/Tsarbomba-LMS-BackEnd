package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.dto.TokenPair;
import com.wanted.codebombalms.auth.application.usecase.LoginUseCase;
import com.wanted.codebombalms.auth.presentation.api.dto.request.LoginRequest;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth - 인증", description = "회원가입 / 로그인 / 토큰 관리 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class LoginController {

    private final LoginUseCase loginUseCase;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(
            summary = "로그인",
            description = "이메일/비밀번호 검증 후 accessToken/refreshToken 쿠키 2개 발급. 단일 세션 강제 + 로그인 이력 저장."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공 — 쿠키 2개 발급")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-001 이메일 또는 비밀번호 불일치")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "USR-007 계정 잠금 상태")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        TokenPair pair = loginUseCase.login(request.toCommand(), httpRequest);

        // 쿠키 2개 설정
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
                AuthResponseCode.LOGIN_COMPLETED,
                AuthResponseMessage.LOGIN_COMPLETED
        ));
    }

    private Cookie createCookie(String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);   // 운영 배포 시 true 로
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}