package com.wanted.codebombalms.admin.permission.infrastructure.persistence;

import com.wanted.codebombalms.admin.permission.domain.model.AdminPermissionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SpringDataAdminPermissionRepository extends JpaRepository<AdminPermissionJpaEntity, Long> {

    boolean existsByAdminUserIdAndPermissionType(
            Long adminUserId,
            AdminPermissionType permissionType
    );

    List<AdminPermissionJpaEntity> findAllByAdminUserId(Long adminUserId);

    List<AdminPermissionJpaEntity> findAllByAdminUserIdIn(Collection<Long> adminUserIds);

    Optional<AdminPermissionJpaEntity> findByAdminUserIdAndPermissionType(
            Long adminUserId,
            AdminPermissionType permissionType
    );
}
