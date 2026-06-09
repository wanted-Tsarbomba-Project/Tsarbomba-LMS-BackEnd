package com.wanted.codebombalms.user.presentation.api;

import com.wanted.codebombalms.auth.presentation.api.support.AuthCookieFactory;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.user.application.usecase.WithdrawUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User - 마이페이지", description = "로그인 사용자의 프로필 조회/수정 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class WithdrawUserController {

    private final WithdrawUserUseCase withdrawUserUseCase;
    private final AuthCookieFactory authCookieFactory;

    @Operation(
            summary = "회원 탈퇴",
            description = "본인 계정 Soft Delete + Refresh Token 전체 삭제 + accessToken/refreshToken 쿠키 만료(Max-Age=0)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "탈퇴 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-016 인증이 필요합니다 (미로그인)")
    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal Long userId,
            HttpServletResponse response
    ) {
        // 1. Soft Delete + RT 전체 삭제
        withdrawUserUseCase.withdraw(userId);

        // 2. 쿠키 만료
        response.addCookie(authCookieFactory.expired("accessToken"));
        response.addCookie(authCookieFactory.expired("refreshToken"));

        return ResponseEntity.ok(ApiResponse.success(
                UserResponseCode.WITHDRAWN,
                UserResponseMessage.WITHDRAWN
        ));
    }
}
