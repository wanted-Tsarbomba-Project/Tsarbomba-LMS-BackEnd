package com.wanted.codebombalms.user.application.query;

import com.wanted.codebombalms.user.domain.model.UserRole;

public record UpdateMyInfoResult(
        boolean nicknameChanged,
        String nickname,
        UserRole role
) {
}
