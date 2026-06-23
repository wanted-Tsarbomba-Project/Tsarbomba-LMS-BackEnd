package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.usecase.GetSocialTempInfoUseCase;
import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.auth.domain.model.OAuthTempData;
import com.wanted.codebombalms.auth.domain.repository.TempTokenRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetSocialTempInfoService implements GetSocialTempInfoUseCase {

    private final TempTokenRepository tempTokenRepository;

    @Override
    public OAuthTempData getTempInfo(String tempToken) {
        // TEMP_TOKEN 조회 (삭제 X). 없거나 만료 시 401
        return tempTokenRepository.find(tempToken)
                .orElseThrow(() -> new UnauthorizedException(AuthErrorCode.AUTH_TEMP_TOKEN_INVALID));
    }
}
