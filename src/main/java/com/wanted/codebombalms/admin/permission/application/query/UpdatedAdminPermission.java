package com.wanted.codebombalms.admin.permission.application.query;

import com.wanted.codebombalms.admin.permission.domain.model.AdminPermissionType;

// 이번 요청으로 변경을 시도한 단일 권한과 최종 부여 여부를 담는다.
public record UpdatedAdminPermission(
        AdminPermissionType permissionType,
        boolean granted
) {
}
