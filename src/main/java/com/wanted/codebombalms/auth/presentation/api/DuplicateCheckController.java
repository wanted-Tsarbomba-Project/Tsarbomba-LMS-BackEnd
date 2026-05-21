package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.usecase.DuplicateCheckUseCase;
import com.wanted.codebombalms.auth.presentation.api.dto.response.AvailabilityResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/check")
@RequiredArgsConstructor
public class DuplicateCheckController {

    private final DuplicateCheckUseCase duplicateCheckUseCase;

    @GetMapping("/email")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkEmail(
            @RequestParam String email
    ) {
        boolean available = duplicateCheckUseCase.isEmailAvailable(email);

        return ResponseEntity.ok(ApiResponse.success(
                AuthResponseCode.EMAIL_AVAILABILITY_CHECKED,
                AuthResponseMessage.EMAIL_AVAILABILITY_CHECKED,
                new AvailabilityResponse(available)
        ));
    }

    @GetMapping("/nickname")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkNickname(
            @RequestParam String nickname
    ) {
        boolean available = duplicateCheckUseCase.isNicknameAvailable(nickname);

        return ResponseEntity.ok(ApiResponse.success(
                AuthResponseCode.NICKNAME_AVAILABILITY_CHECKED,
                AuthResponseMessage.NICKNAME_AVAILABILITY_CHECKED,
                new AvailabilityResponse(available)
        ));
    }
}