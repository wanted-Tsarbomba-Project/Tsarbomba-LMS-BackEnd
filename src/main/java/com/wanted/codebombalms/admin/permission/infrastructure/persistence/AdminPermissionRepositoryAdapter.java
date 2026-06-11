package com.wanted.codebombalms.admin.permission.infrastructure.persistence;

import com.wanted.codebombalms.admin.permission.domain.model.AdminPermissionType;
import com.wanted.codebombalms.admin.permission.domain.repository.AdminPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminPermissionRepositoryAdapter implements AdminPermissionRepository {

    private final SpringDataAdminPermissionRepository springDataRepository;

    @Override
    public boolean existsByAdminUserIdAndPermissionType(
            Long adminUserId,
            AdminPermissionType permissionType
    ) {
        return springDataRepository.existsByAdminUserIdAndPermissionType(
                adminUserId,
                permissionType
        );
    }
}
