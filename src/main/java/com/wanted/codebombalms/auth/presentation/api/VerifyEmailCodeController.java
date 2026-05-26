package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.usecase.VerifyEmailCodeUseCase;
import com.wanted.codebombalms.auth.presentation.api.dto.request.VerifyEmailCodeRequest;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/email")
@RequiredArgsConstructor
public class VerifyEmailCodeController {

    private final VerifyEmailCodeUseCase verifyEmailCodeUseCase;

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verify(
            @Valid @RequestBody VerifyEmailCodeRequest request
    ) {
        verifyEmailCodeUseCase.verifyCode(request.email(), request.code());

        return ResponseEntity.ok(ApiResponse.success(
                AuthResponseCode.EMAIL_VERIFIED,
                AuthResponseMessage.EMAIL_VERIFIED
        ));
    }
}