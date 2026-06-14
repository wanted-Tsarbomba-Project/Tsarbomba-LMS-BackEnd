package com.wanted.codebombalms.admin.permission.application.command;

import com.wanted.codebombalms.admin.permission.domain.model.AdminPermissionType;

// master가 특정 admin 계정의 단일 권한을 부여하거나 회수할 때 사용하는 command다.
public record ToggleAdminPermissionCommand(
        Long adminUserId,
        AdminPermissionType permissionType,
        boolean granted,
        Long grantedBy
) {
}
