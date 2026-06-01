package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.usecase.DuplicateCheckUseCase;
import com.wanted.codebombalms.auth.presentation.api.dto.response.AvailabilityResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth - 인증", description = "회원가입 / 로그인 / 토큰 관리 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/auth/check")
@RequiredArgsConstructor
public class DuplicateCheckController {

    private final DuplicateCheckUseCase duplicateCheckUseCase;

    @Operation(
            summary = "이메일 중복 확인",
            description = "이메일 사용 가능 여부 조회. 사용 가능/중복 모두 200 응답, data.available 로 구분."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "확인 성공 (available: true=사용 가능 / false=이미 사용 중)")
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

    @Operation(
            summary = "닉네임 중복 확인",
            description = "닉네임 사용 가능 여부 조회. 사용 가능/중복 모두 200 응답, data.available 로 구분."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "확인 성공 (available: true=사용 가능 / false=이미 사용 중)")
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