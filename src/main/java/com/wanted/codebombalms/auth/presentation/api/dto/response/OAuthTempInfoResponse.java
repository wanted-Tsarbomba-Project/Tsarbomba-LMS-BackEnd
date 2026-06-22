package com.wanted.codebombalms.auth.presentation.api.dto.response;

import com.wanted.codebombalms.auth.domain.model.OAuthTempData;

public record OAuthTempInfoResponse(
        String email,
        String name
) {
    public static OAuthTempInfoResponse from(OAuthTempData data) {
        return new OAuthTempInfoResponse(data.email(), data.name());
    }
}
