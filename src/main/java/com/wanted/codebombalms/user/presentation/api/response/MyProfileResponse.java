package com.wanted.codebombalms.user.presentation.api.response;

import com.wanted.codebombalms.user.application.query.MyProfileResult;

public record MyProfileResponse(
        String email,
        String name,
        String nickname,
        String phone,
        String role,
        String provider,
        boolean emailVerified
) {

    public static MyProfileResponse from(MyProfileResult result) {
        return new MyProfileResponse(
                result.email(),
                result.name(),
                result.nickname(),
                result.phone(),
                result.role().name(),
                result.provider().name(),
                result.emailVerified()
        );
    }
}
