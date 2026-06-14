package com.wanted.codebombalms.user.presentation.api;

import com.wanted.codebombalms.auth.presentation.api.support.AuthCookieFactory;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.user.application.usecase.ChangePasswordUseCase;
import com.wanted.codebombalms.user.presentation.api.request.ChangePasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User - 마이페이지", description = "로그인 사용자의 프로필 조회/수정 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class ChangePasswordController {

    private final ChangePasswordUseCase changePasswordUseCase;
    private final AuthCookieFactory authCookieFactory;

    @Operation(
            summary = "비밀번호 변경",
            description = "새 비밀번호로 변경 후 Refresh Token 전체 삭제 + accessToken/refreshToken 쿠키 만료(강제 재로그인)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공 (강제 재로그인)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "USR-004 형식 오류 / USR-006 확인 불일치")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-016 인증이 필요합니다 (미로그인)")
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletResponse response
    ) {
        // 1. 비밀번호 변경 + RT 전체 삭제
        changePasswordUseCase.changePassword(userId, request.newPassword(), request.confirmPassword());

        // 2. 쿠키 만료 (강제 재로그인)
        response.addCookie(authCookieFactory.expired("accessToken"));
        response.addCookie(authCookieFactory.expired("refreshToken"));

        return ResponseEntity.ok(ApiResponse.success(
                UserResponseCode.PASSWORD_CHANGED,
                UserResponseMessage.PASSWORD_CHANGED
        ));
    }
}
