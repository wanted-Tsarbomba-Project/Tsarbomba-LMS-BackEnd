package com.wanted.codebombalms.user.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.user.application.usecase.VerifyPasswordUseCase;
import com.wanted.codebombalms.user.presentation.api.request.VerifyPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User - 마이페이지", description = "로그인 사용자의 프로필 조회/수정 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class VerifyPasswordController {

    private final VerifyPasswordUseCase verifyPasswordUseCase;

    @Operation(
            summary = "개인정보 수정 비밀번호 인증",
            description = "개인정보 수정 진입 전 현재 비밀번호를 재확인한다. (세션 탈취/자리 비움 중 무단 수정 방지)"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "AUT-013 비밀번호 불일치")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-016 인증이 필요합니다 (미로그인)")
    @PostMapping("/me/verify-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> verifyPassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody VerifyPasswordRequest request
    ) {
        verifyPasswordUseCase.verify(userId, request.password());

        return ResponseEntity.ok(ApiResponse.success(
                UserResponseCode.PASSWORD_VERIFIED,
                UserResponseMessage.PASSWORD_VERIFIED
        ));
    }
}
