package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.usecase.LogoutUseCase;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth - 인증", description = "회원가입 / 로그인 / 토큰 관리 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class LogoutController {

    private final LogoutUseCase logoutUseCase;

    @Operation(
            summary = "로그아웃",
            description = "DB의 Refresh Token 삭제 + accessToken/refreshToken 쿠키 만료(Max-Age=0). 인증된 사용자만 호출 가능."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-016 인증이 필요합니다 (미로그인)")
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal Long userId,
            HttpServletResponse response
    ) {
        // 1. DB의 Refresh Token 삭제
        logoutUseCase.logout(userId);

        // 2. 쿠키 만료
        response.addCookie(createExpiredCookie("accessToken"));
        response.addCookie(createExpiredCookie("refreshToken"));

        return ResponseEntity.ok(ApiResponse.success(
                AuthResponseCode.LOGOUT_COMPLETED,
                AuthResponseMessage.LOGOUT_COMPLETED
        ));
    }

    private Cookie createExpiredCookie(String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);   // ← 즉시 만료
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}