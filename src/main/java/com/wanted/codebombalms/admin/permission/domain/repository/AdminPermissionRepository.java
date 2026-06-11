package com.wanted.codebombalms.admin.permission.domain.repository;

import com.wanted.codebombalms.admin.permission.domain.model.AdminPermissionType;

public interface AdminPermissionRepository {

    boolean existsByAdminUserIdAndPermissionType(
            Long adminUserId,
            AdminPermissionType permissionType
    );
}
