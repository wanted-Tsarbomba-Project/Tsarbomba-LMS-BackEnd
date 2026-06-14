package com.wanted.codebombalms.admin.permission.application.query;

import com.wanted.codebombalms.user.domain.model.UserRole;

import java.time.LocalDateTime;

// master 화면의 admin 목록 한 행에 필요한 계정 정보와 권한 상태를 담는다.
public record AdminAccountSummary(
        Long userId,
        String email,
        String name,
        String nickname,
        UserRole role,
        boolean locked,
        AdminPermissionStates permissionStates,
        LocalDateTime createdAt
) {
}
