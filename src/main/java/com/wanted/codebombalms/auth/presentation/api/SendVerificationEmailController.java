package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.usecase.SendVerificationEmailUseCase;
import com.wanted.codebombalms.auth.presentation.api.dto.request.SendVerificationEmailRequest;
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
public class SendVerificationEmailController {

    private final SendVerificationEmailUseCase sendVerificationEmailUseCase;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> send(
            @Valid @RequestBody SendVerificationEmailRequest request
    ) {
        sendVerificationEmailUseCase.sendVerificationCode(request.email());

        return ResponseEntity.ok(ApiResponse.success(
                AuthResponseCode.EMAIL_VERIFICATION_SENT,
                AuthResponseMessage.EMAIL_VERIFICATION_SENT
        ));
    }
}