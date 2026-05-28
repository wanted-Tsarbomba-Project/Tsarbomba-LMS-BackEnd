package com.wanted.codebombalms.user.application.query;

import com.wanted.codebombalms.user.domain.model.User;
import java.time.LocalDateTime;

public record StudentSummary(
        Long userId,
        String email,
        String nickname,
        boolean isLocked,
        LocalDateTime createdAt
) {

    public static StudentSummary from(User user) {
        return new StudentSummary(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.isLocked(),
                user.getCreatedAt()
        );
    }
}
