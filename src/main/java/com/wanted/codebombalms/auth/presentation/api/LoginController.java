package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.dto.LoginResult;
import com.wanted.codebombalms.auth.application.usecase.LoginUseCase;
import com.wanted.codebombalms.auth.presentation.api.dto.request.LoginRequest;
import com.wanted.codebombalms.auth.presentation.api.dto.response.LoginResponse;
import com.wanted.codebombalms.auth.presentation.api.support.AuthCookieFactory;
import com.wanted.codebombalms.auth.presentation.api.support.DeviceFingerprintResolver;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private static final int STEP_UP_COOKIE_MAX_AGE = 300; // 5분 (step-up TTL과 동일)

    private final LoginUseCase loginUseCase;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCookieFactory authCookieFactory;
    private final DeviceFingerprintResolver deviceFingerprintResolver;

    @Operation(
            summary = "로그인",
            description = "이메일/비밀번호 검증 후 적응형 인증. 신뢰 기기면 토큰 2개 발급, 미신뢰/위치급변이면 stepupToken 발급 후 이메일 OTP 추가 인증 필요(data.stepUpRequired=true)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공 또는 추가 인증 필요 (data.stepUpRequired 로 분기)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-001 이메일 또는 비밀번호 불일치")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "USR-007 계정 잠금 상태")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        String deviceFp = deviceFingerprintResolver.resolve(httpRequest, response);
        LoginResult result = loginUseCase.login(request.toCommand(), httpRequest, deviceFp);

        // 미신뢰 기기 → stepupToken 쿠키만 발급 (정식 토큰 보류)
        if (result.stepUpRequired()) {
            response.addCookie(authCookieFactory.create("stepupToken", result.stepUpToken(), STEP_UP_COOKIE_MAX_AGE));
            return ResponseEntity.ok(ApiResponse.success(
                    AuthResponseCode.STEP_UP_REQUIRED,
                    AuthResponseMessage.STEP_UP_REQUIRED,
                    LoginResponse.from(result)
            ));
        }

        // 신뢰 기기 → 정식 토큰 쿠키 2개
        response.addCookie(authCookieFactory.create(
                "accessToken", result.accessToken(), (int) (jwtTokenProvider.getAccessExpiration() / 1000)));
        response.addCookie(authCookieFactory.create(
                "refreshToken", result.refreshToken(), (int) (jwtTokenProvider.getRefreshExpiration() / 1000)));
        return ResponseEntity.ok(ApiResponse.success(
                AuthResponseCode.LOGIN_COMPLETED,
                AuthResponseMessage.LOGIN_COMPLETED,
                LoginResponse.from(result)
        ));
    }
}
