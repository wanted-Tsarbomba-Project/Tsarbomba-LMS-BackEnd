package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.dto.SessionStatus;
import com.wanted.codebombalms.auth.application.dto.TokenPair;
import com.wanted.codebombalms.auth.application.usecase.SessionQueryUseCase;
import com.wanted.codebombalms.auth.application.usecase.TokenReissueUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.presentation.api.dto.response.SessionStatusResponse;
import com.wanted.codebombalms.auth.presentation.api.support.AuthCookieFactory;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

@Tag(name = "Auth - 인증", description = "회원가입 / 로그인 / 토큰 관리 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class SessionController {

    private static final String COOKIE_ACCESS_TOKEN = "accessToken";
    private static final String COOKIE_REFRESH_TOKEN = "refreshToken";

    private final SessionQueryUseCase sessionQueryUseCase;
    private final TokenReissueUseCase tokenReissueUseCase;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCookieFactory authCookieFactory;

    @Operation(
            summary = "세션 잔여 시간 조회",
            description = "현재 세션의 남은 시간(초)과 만료 시각을 반환. AT/RT 모두 HttpOnly 쿠키라 프론트가 직접 exp 를 읽을 수 없어 서버가 계산해준다. 자동 로그아웃 안내 모달용."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-016 인증이 필요합니다 (미로그인)")
    @GetMapping("/session")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SessionStatusResponse>> status(HttpServletRequest request) {

        SessionStatus status = sessionQueryUseCase.query(
                cookieValue(request, COOKIE_ACCESS_TOKEN),
                cookieValue(request, COOKIE_REFRESH_TOKEN)
        );

        return ResponseEntity.ok(ApiResponse.success(
                AuthResponseCode.SESSION_STATUS_RETRIEVED,
                AuthResponseMessage.SESSION_STATUS_RETRIEVED,
                SessionStatusResponse.from(status)
        ));
    }

    @Operation(
            summary = "세션 연장",
            description = "refreshToken 쿠키로 accessToken/refreshToken 을 재발급해 세션을 연장한다(RTR — 기존 RT 즉시 폐기). "
                    + "동작은 /reissue 와 동일하되, 사용자가 직접 '연장하기'를 누른 흐름이라 응답으로 갱신된 잔여 시간을 함께 준다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "연장 성공 — 새 쿠키 2개 + 갱신된 잔여 시간")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-004 유효하지 않은 Refresh Token / AUT-005 만료된 Refresh Token")
    @PostMapping("/session/extend")
    public ResponseEntity<ApiResponse<SessionStatusResponse>> extend(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 1. 쿠키에서 refreshToken 추출
        String refreshToken = cookieValue(request, COOKIE_REFRESH_TOKEN);
        if (refreshToken == null) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_REFRESH_TOKEN_INVALID);
        }

        // 2. 재발급(RTR) — sid 회전까지 기존 로직 그대로 재사용
        TokenPair pair = tokenReissueUseCase.reissue(refreshToken);

        // 3. 새 쿠키 2개 발급 (기존 쿠키 덮어쓰기)
        issueTokenCookies(response, pair);

        // 4. 갱신된 잔여 시간 응답 — 프론트가 타이머를 곧바로 리셋할 수 있게
        SessionStatus status = sessionQueryUseCase.query(pair.accessToken(), pair.refreshToken());

        return ResponseEntity.ok(ApiResponse.success(
                AuthResponseCode.SESSION_EXTENDED,
                AuthResponseMessage.SESSION_EXTENDED,
                SessionStatusResponse.from(status)
        ));
    }

    /** 재발급된 토큰 쌍을 쿠키로 내려준다 (기존 쿠키 덮어쓰기). */
    private void issueTokenCookies(HttpServletResponse response, TokenPair pair) {
        response.addCookie(authCookieFactory.create(
                COOKIE_ACCESS_TOKEN,
                pair.accessToken(),
                (int) (jwtTokenProvider.getAccessExpiration() / 1000)
        ));
        response.addCookie(authCookieFactory.create(
                COOKIE_REFRESH_TOKEN,
                pair.refreshToken(),
                (int) (jwtTokenProvider.getRefreshExpiration() / 1000)
        ));
    }

    private String cookieValue(HttpServletRequest request, String name) {
        Cookie cookie = WebUtils.getCookie(request, name);
        return cookie == null ? null : cookie.getValue();
    }
}
