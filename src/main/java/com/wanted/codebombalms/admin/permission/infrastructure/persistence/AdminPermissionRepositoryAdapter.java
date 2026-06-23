package com.wanted.codebombalms.admin.permission.infrastructure.persistence;

import com.wanted.codebombalms.admin.permission.domain.model.AdminPermissionType;
import com.wanted.codebombalms.admin.permission.domain.repository.AdminPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    @Override
    public List<AdminPermissionType> findPermissionTypesByAdminUserId(Long adminUserId) {
        return springDataRepository.findAllByAdminUserId(adminUserId)
                .stream()
                .map(AdminPermissionJpaEntity::getPermissionType)
                .toList();
    }

    @Override
    public void grant(Long adminUserId, AdminPermissionType permissionType, Long grantedBy) {
        if (existsByAdminUserIdAndPermissionType(adminUserId, permissionType)) {
            return;
        }

        try {
            springDataRepository.saveAndFlush(new AdminPermissionJpaEntity(
                    adminUserId,
                    permissionType,
                    grantedBy
            ));
        } catch (DataIntegrityViolationException e) {
            if (existsByAdminUserIdAndPermissionType(adminUserId, permissionType)) {
                return;
            }
            throw e;
        }
    }

    @Override
    public void revoke(Long adminUserId, AdminPermissionType permissionType) {
        springDataRepository.findByAdminUserIdAndPermissionType(adminUserId, permissionType)
                .ifPresent(springDataRepository::delete);
    }
}
