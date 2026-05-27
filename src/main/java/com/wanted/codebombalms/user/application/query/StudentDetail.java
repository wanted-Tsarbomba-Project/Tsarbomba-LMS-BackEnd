package com.wanted.codebombalms.user.application.query;

import com.wanted.codebombalms.user.domain.model.AuthProvider;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;

import java.time.LocalDateTime;

public record StudentDetail(
        Long userId,
        String email,
        String name,
        String nickname,
        String phone,
        UserRole role,
        AuthProvider provider,
        boolean isLocked,
        LocalDateTime createdAt
) {

    public static StudentDetail from(User user) {
        return new StudentDetail(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getPhone(),
                user.getRole(),
                user.getProvider(),
                user.isLocked(),
                user.getCreatedAt()
        );
    }
}
