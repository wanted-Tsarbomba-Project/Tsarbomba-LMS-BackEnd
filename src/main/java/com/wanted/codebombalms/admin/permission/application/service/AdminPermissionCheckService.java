package com.wanted.codebombalms.admin.permission.application.service;

import com.wanted.codebombalms.admin.permission.domain.exception.AdminAuthErrorCode;
import com.wanted.codebombalms.admin.permission.domain.model.AdminPermissionType;
import com.wanted.codebombalms.admin.permission.domain.repository.AdminPermissionRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPermissionCheckService {

    private final AdminPermissionRepository adminPermissionRepository;

    public void requirePermission(
            Long adminUserId,
            AdminPermissionType permissionType
    ) {
        boolean hasPermission = adminPermissionRepository.existsByAdminUserIdAndPermissionType(
                adminUserId,
                permissionType
        );

        if (!hasPermission) {
            throw new ForbiddenException(toErrorCode(permissionType));
        }
    }

    private AdminAuthErrorCode toErrorCode(AdminPermissionType permissionType) {
        return switch (permissionType) {
            case USER_MANAGEMENT -> AdminAuthErrorCode.USER_MANAGEMENT_PERMISSION_REQUIRED;
            case RULE_MANAGEMENT -> AdminAuthErrorCode.RULE_MANAGEMENT_PERMISSION_REQUIRED;
        };
    }
}
