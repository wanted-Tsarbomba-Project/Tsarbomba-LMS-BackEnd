package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.command.StepUpVerifyCommand;
import com.wanted.codebombalms.auth.application.dto.LoginResult;
import com.wanted.codebombalms.auth.application.usecase.StepUpVerifyUseCase;
import com.wanted.codebombalms.auth.presentation.api.dto.request.StepUpVerifyRequest;
import com.wanted.codebombalms.auth.presentation.api.dto.response.LoginResponse;
import com.wanted.codebombalms.auth.presentation.api.support.AuthCookieFactory;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth - 인증", description = "회원가입 / 로그인 / 토큰 관리 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class StepUpVerifyController {

    private final StepUpVerifyUseCase stepUpVerifyUseCase;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCookieFactory authCookieFactory;

    @Operation(summary = "추가 인증 검증", description = "미신뢰 기기 로그인 시 발급된 stepupToken + 이메일 OTP 검증 → 정식 토큰 발급. trustDevice=true 면 신뢰 기기 등록.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 성공 — 토큰 2개 발급")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "AUT-009 유효하지 않은 인증 코드")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-024 step-up 임시토큰 무효/만료")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "AUT-025 추가 인증 시도 초과")
    @PostMapping("/step-up/verify")
    public ResponseEntity<ApiResponse<LoginResponse>> verify(
            @CookieValue(name = "stepupToken", required = false) String stepUpToken,
            @Valid @RequestBody StepUpVerifyRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        LoginResult result = stepUpVerifyUseCase.verify(
                new StepUpVerifyCommand(stepUpToken, request.code(), request.trustDevice()),
                httpRequest
        );

        // 정식 토큰 발급 + stepupToken 만료
        response.addCookie(authCookieFactory.create(
                "accessToken", result.accessToken(), (int) (jwtTokenProvider.getAccessExpiration() / 1000)));
        response.addCookie(authCookieFactory.create(
                "refreshToken", result.refreshToken(), (int) (jwtTokenProvider.getRefreshExpiration() / 1000)));
        response.addCookie(authCookieFactory.expired("stepupToken"));

        return ResponseEntity.ok(ApiResponse.success(
                AuthResponseCode.STEP_UP_VERIFIED,
                AuthResponseMessage.STEP_UP_VERIFIED,
                LoginResponse.from(result)
        ));
    }
}
