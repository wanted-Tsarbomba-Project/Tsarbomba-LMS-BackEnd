package com.wanted.codebombalms.admin.permission.domain.repository;

import com.wanted.codebombalms.admin.permission.domain.model.AdminPermissionType;

import java.util.List;

public interface AdminPermissionRepository {

    // admin 계정이 특정 권한 row를 가지고 있는지 확인한다.
    boolean existsByAdminUserIdAndPermissionType(
            Long adminUserId,
            AdminPermissionType permissionType
    );

    // admin 계정이 가진 모든 권한 타입을 조회한다.
    List<AdminPermissionType> findPermissionTypesByAdminUserId(Long adminUserId);

    // 권한 row가 없으면 생성하고, 이미 있으면 그대로 둔다.
    void grant(Long adminUserId, AdminPermissionType permissionType, Long grantedBy);

    // 권한 row가 있으면 삭제하고, 이미 없으면 그대로 둔다.
    void revoke(Long adminUserId, AdminPermissionType permissionType);
}
