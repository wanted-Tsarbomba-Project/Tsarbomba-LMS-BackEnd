package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.usecase.GetSocialTempInfoUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.model.OAuthTempData;
import com.wanted.codebombalms.auth.presentation.api.dto.response.OAuthTempInfoResponse;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth - 소셜 로그인", description = "구글 OAuth2 로그인 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/auth/oauth2")
@RequiredArgsConstructor
public class OAuthTempInfoController {

    private final GetSocialTempInfoUseCase getSocialTempInfoUseCase;

    @Operation(
            summary = "소셜 임시정보 조회",
            description = "추가정보 페이지에서 TEMP_TOKEN 으로 구글 email/name 을 조회해 표시용으로 반환. (삭제 X)"
    )
    @GetMapping("/temp-info")
    public ResponseEntity<ApiResponse<OAuthTempInfoResponse>> getTempInfo(
            @CookieValue(name = "tempToken", required = false) String tempToken
    ) {
        if (tempToken == null || tempToken.isBlank()) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_TEMP_TOKEN_INVALID);
        }

        OAuthTempData data = getSocialTempInfoUseCase.getTempInfo(tempToken);

        return ResponseEntity.ok(ApiResponse.success(
                AuthResponseCode.OAUTH_TEMP_INFO_RETRIEVED,
                AuthResponseMessage.OAUTH_TEMP_INFO_RETRIEVED,
                OAuthTempInfoResponse.from(data)
        ));
    }
}
