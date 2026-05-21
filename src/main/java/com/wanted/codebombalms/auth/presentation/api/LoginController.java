package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.dto.TokenPair;
import com.wanted.codebombalms.auth.application.usecase.LoginUseCase;
import com.wanted.codebombalms.auth.presentation.api.dto.request.LoginRequest;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class LoginController {

    private final LoginUseCase loginUseCase;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        TokenPair pair = loginUseCase.login(request.toCommand());

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