package com.wanted.codebombalms.user.presentation.api;

import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.user.application.query.UpdateMyInfoResult;
import com.wanted.codebombalms.user.application.usecase.UpdateMyInfoUseCase;
import com.wanted.codebombalms.user.presentation.api.request.UpdateMyInfoRequest;
import com.wanted.codebombalms.auth.presentation.api.support.AuthCookieFactory;
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
public class UpdateMyInfoController {

    private final UpdateMyInfoUseCase updateMyInfoUseCase;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCookieFactory authCookieFactory;

    @Operation(
            summary = "개인정보 수정",
            description = "닉네임/전화번호를 수정한다. 닉네임이 변경되면 JWT 일관성을 위해 accessToken 쿠키를 재발급한다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공 (닉네임 변경 시 accessToken 쿠키 재발급)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "USR-003 닉네임 중복 / USR-005 전화번호 형식 오류")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-016 인증이 필요합니다 (미로그인)")
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateMyInfo(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateMyInfoRequest request,
            HttpServletResponse response
    ) {
        // 1. 개인정보 수정 (닉네임 변경 여부 반환)
        UpdateMyInfoResult result = updateMyInfoUseCase.update(userId, request.nickname(), request.phone());

        // 2. 닉네임 변경 시에만 accessToken 재발급 (JWT nickname claim 최신화)
        if (result.nicknameChanged()) {
            String newAccessToken = jwtTokenProvider.generateAccessToken(
                    userId, result.nickname(), result.role()
            );
            response.addCookie(authCookieFactory.create(
                    "accessToken",
                    newAccessToken,
                    (int) (jwtTokenProvider.getAccessExpiration() / 1000)
            ));
        }

        return ResponseEntity.ok(ApiResponse.success(
                UserResponseCode.PROFILE_UPDATED,
                UserResponseMessage.PROFILE_UPDATED
        ));
    }
}
