package com.wanted.codebombalms.admin.permission.application.query;

import com.wanted.codebombalms.user.domain.model.UserRole;

// 권한 부여/회수 후 admin 계정의 현재 권한 상태를 담는다.
public record AdminAccountPermissionResult(
        Long userId,
        String email,
        String name,
        String nickname,
        UserRole role,
        AdminPermissionStates permissionStates,
        UpdatedAdminPermission updatedPermission
) {
}
