package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.usecase.LogoutUseCase;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class LogoutController {

    private final LogoutUseCase logoutUseCase;

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