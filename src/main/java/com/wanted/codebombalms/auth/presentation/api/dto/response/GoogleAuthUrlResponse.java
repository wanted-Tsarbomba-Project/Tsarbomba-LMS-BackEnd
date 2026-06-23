package com.wanted.codebombalms.auth.presentation.api.dto.response;

public record GoogleAuthUrlResponse(
        String authorizationUri
) {
    public static GoogleAuthUrlResponse of(String authorizationUri) {
        return new GoogleAuthUrlResponse(authorizationUri);
    }
}
