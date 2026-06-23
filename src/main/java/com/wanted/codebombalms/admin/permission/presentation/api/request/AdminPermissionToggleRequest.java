package com.wanted.codebombalms.admin.permission.presentation.api.request;

import com.wanted.codebombalms.admin.permission.application.command.ToggleAdminPermissionCommand;
import com.wanted.codebombalms.admin.permission.domain.exception.AdminAuthErrorCode;
import com.wanted.codebombalms.admin.permission.domain.model.AdminPermissionType;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.Getter;
import lombok.NoArgsConstructor;

// master가 admin 계정의 단일 권한을 부여하거나 회수할 때 받는 JSON request body다.
@Getter
@NoArgsConstructor
public class AdminPermissionToggleRequest {

    private String permissionType;
    private Boolean granted;

    // path variable과 로그인한 master id를 합쳐 application command로 변환한다.
    public ToggleAdminPermissionCommand toCommand(Long adminUserId, Long grantedBy) {
        return new ToggleAdminPermissionCommand(
                adminUserId,
                parsePermissionType(),
                parseGranted(),
                grantedBy
        );
    }

    // request body의 permissionType 문자열을 domain enum으로 변환한다.
    private AdminPermissionType parsePermissionType() {
        if (permissionType == null || permissionType.isBlank()) {
            throw new ValidationException(AdminAuthErrorCode.INVALID_ADMIN_PERMISSION_REQUEST);
        }

        try {
            return AdminPermissionType.valueOf(permissionType);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(AdminAuthErrorCode.INVALID_ADMIN_PERMISSION_REQUEST);
        }
    }

    // granted가 누락되지 않았는지 확인하고 primitive boolean으로 변환한다.
    private boolean parseGranted() {
        if (granted == null) {
            throw new ValidationException(AdminAuthErrorCode.INVALID_ADMIN_PERMISSION_REQUEST);
        }
        return granted;
    }
}
