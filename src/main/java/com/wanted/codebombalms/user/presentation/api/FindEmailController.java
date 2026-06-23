package com.wanted.codebombalms.user.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.user.application.usecase.FindEmailUseCase;
import com.wanted.codebombalms.user.presentation.api.request.FindEmailRequest;
import com.wanted.codebombalms.user.presentation.api.response.FindEmailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User - 이메일 찾기", description = "이름 + 전화번호로 가입 이메일 조회 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class FindEmailController {

    private final FindEmailUseCase findEmailUseCase;

    @Operation(
            summary = "이메일 찾기",
            description = "이름 + 전화번호가 일치하는 회원의 이메일을 마스킹 처리하여 반환. (탈퇴 회원 제외)"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USR-001 일치하는 회원 없음")
    @PostMapping("/find-email")
    public ResponseEntity<ApiResponse<FindEmailResponse>> findEmail(
            @Valid @RequestBody FindEmailRequest request
    ) {
        String maskedEmail = findEmailUseCase.findEmail(request.name(), request.phone());

        return ResponseEntity.ok(ApiResponse.success(
                UserResponseCode.EMAIL_FOUND,
                UserResponseMessage.EMAIL_FOUND,
                FindEmailResponse.of(maskedEmail)
        ));
    }
}
