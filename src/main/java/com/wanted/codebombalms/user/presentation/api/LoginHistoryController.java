package com.wanted.codebombalms.user.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.user.application.usecase.GetLoginHistoryUseCase;
import com.wanted.codebombalms.user.presentation.api.response.LoginHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "User - 마이페이지", description = "로그인 사용자의 프로필 조회/수정 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class LoginHistoryController {

    private final GetLoginHistoryUseCase getLoginHistoryUseCase;

    @Operation(summary = "로그인 이력 조회", description = "로그인 사용자의 로그인 이력(IP/지역/의심여부) 최신순 조회. 페이지당 20건.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-016 인증이 필요합니다 (미로그인)")
    @GetMapping("/me/login-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LoginHistoryResponse>>> getLoginHistory(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page
    ) {
        List<LoginHistoryResponse> history = getLoginHistoryUseCase.getLoginHistory(userId, page).stream()
                .map(LoginHistoryResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                UserResponseCode.LOGIN_HISTORY_RETRIEVED,
                UserResponseMessage.LOGIN_HISTORY_RETRIEVED,
                history
        ));
    }
}
