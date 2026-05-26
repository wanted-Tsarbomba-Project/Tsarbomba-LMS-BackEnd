package com.wanted.codebombalms.user.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.user.application.query.MyProfileResult;
import com.wanted.codebombalms.user.application.usecase.GetMyProfileUseCase;
import com.wanted.codebombalms.user.presentation.api.dto.response.MyProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class MyProfileController {

    private final GetMyProfileUseCase getMyProfileUseCase;

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