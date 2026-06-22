package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.usecase.GoogleLoginStartUseCase;
import com.wanted.codebombalms.auth.presentation.api.dto.response.GoogleAuthUrlResponse;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth - 소셜 로그인", description = "구글 OAuth2 로그인 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/auth/oauth2")
@RequiredArgsConstructor
public class OAuthStartController {

    private final GoogleLoginStartUseCase googleLoginStartUseCase;

    @Operation(
            summary = "구글 로그인 시작",
            description = "state 발급 후 구글 OAuth 동의 화면 URL 을 반환. 프론트가 해당 URL 로 이동한다."
    )
    @GetMapping("/google")
    public ResponseEntity<ApiResponse<GoogleAuthUrlResponse>> startGoogleLogin() {
        String authorizationUri = googleLoginStartUseCase.createAuthorizationUri();

        return ResponseEntity.ok(ApiResponse.success(
                AuthResponseCode.OAUTH_AUTH_URL_ISSUED,
                AuthResponseMessage.OAUTH_AUTH_URL_ISSUED,
                GoogleAuthUrlResponse.of(authorizationUri)
        ));
    }
}
