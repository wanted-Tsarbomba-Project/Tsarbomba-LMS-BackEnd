package com.wanted.codebombalms.admin.permission.infrastructure.persistence;

import com.wanted.codebombalms.admin.permission.domain.model.AdminPermissionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAdminPermissionRepository extends JpaRepository<AdminPermissionJpaEntity, Long> {

    boolean existsByAdminUserIdAndPermissionType(
            Long adminUserId,
            AdminPermissionType permissionType
    );
}
