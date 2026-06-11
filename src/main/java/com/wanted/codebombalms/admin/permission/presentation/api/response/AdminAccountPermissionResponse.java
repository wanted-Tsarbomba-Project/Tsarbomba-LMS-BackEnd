package com.wanted.codebombalms.admin.permission.presentation.api.response;

import com.wanted.codebombalms.admin.permission.application.query.AdminAccountPermissionResult;

// 권한 변경 후 대상 admin 계정의 현재 권한 상태를 내려준다.
public record AdminAccountPermissionResponse(
        Long userId,
        String email,
        String name,
        String nickname,
        String role,
        AdminPermissionStatesResponse permissionStates,
        UpdatedAdminPermissionResponse updatedPermission
) {

    // application 처리 결과를 API response로 변환한다.
    public static AdminAccountPermissionResponse from(AdminAccountPermissionResult result) {
        return new AdminAccountPermissionResponse(
                result.userId(),
                result.email(),
                result.name(),
                result.nickname(),
                result.role().name(),
                AdminPermissionStatesResponse.from(result.permissionStates()),
                UpdatedAdminPermissionResponse.from(result.updatedPermission())
        );
    }
}
