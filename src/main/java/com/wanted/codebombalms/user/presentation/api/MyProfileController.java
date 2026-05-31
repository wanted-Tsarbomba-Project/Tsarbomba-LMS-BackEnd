package com.wanted.codebombalms.user.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.user.application.query.MyProfileResult;
import com.wanted.codebombalms.user.application.usecase.GetMyProfileUseCase;
import com.wanted.codebombalms.user.presentation.api.response.MyProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User - 마이페이지", description = "로그인 사용자의 프로필 조회/수정 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class MyProfileController {

    private final GetMyProfileUseCase getMyProfileUseCase;

    @Operation(
            summary = "내 정보 조회",
            description = "로그인 사용자의 프로필 정보 조회 (email, name, nickname, phone, role, provider, emailVerified)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-016 인증이 필요합니다 (미로그인)")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile(
            @AuthenticationPrincipal Long userId
    ) {
        MyProfileResult result = getMyProfileUseCase.getMyProfile(userId);

        return ResponseEntity.ok(ApiResponse.success(
                UserResponseCode.MY_PROFILE_RETRIEVED,
                UserResponseMessage.MY_PROFILE_RETRIEVED,
                MyProfileResponse.from(result)
        ));
    }
}
