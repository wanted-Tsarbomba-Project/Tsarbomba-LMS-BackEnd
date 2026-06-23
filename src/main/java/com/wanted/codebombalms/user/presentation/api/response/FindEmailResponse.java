package com.wanted.codebombalms.user.presentation.api.response;

public record FindEmailResponse(
        String email
) {

    public static FindEmailResponse of(String maskedEmail) {
        return new FindEmailResponse(maskedEmail);
    }
}
