package com.wanted.codebombalms.admin.permission.presentation.api.response;

import com.wanted.codebombalms.admin.permission.application.query.UpdatedAdminPermission;

// 이번 요청으로 변경된 권한 타입과 최종 부여 여부를 담는다.
public record UpdatedAdminPermissionResponse(
        String permissionType,
        boolean granted
) {

    // application 변경 결과를 API response로 변환한다.
    public static UpdatedAdminPermissionResponse from(UpdatedAdminPermission updatedPermission) {
        return new UpdatedAdminPermissionResponse(
                updatedPermission.permissionType().name(),
                updatedPermission.granted()
        );
    }
}
