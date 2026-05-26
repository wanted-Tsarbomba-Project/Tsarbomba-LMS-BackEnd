package com.wanted.codebombalms.user.application.query;

import com.wanted.codebombalms.user.domain.model.AuthProvider;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;

public record MyProfileResult(
        String email,
        String name,
        String nickname,
        String phone,
        UserRole role,
        AuthProvider provider,
        boolean emailVerified
) {

    public static MyProfileResult from(User user) {
        return new MyProfileResult(
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getPhone(),
                user.getRole(),
                user.getProvider(),
                user.isEmailVerified()
        );
    }
}
