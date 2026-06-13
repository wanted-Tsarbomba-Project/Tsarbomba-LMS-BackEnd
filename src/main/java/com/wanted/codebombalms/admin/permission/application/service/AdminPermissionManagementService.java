package com.wanted.codebombalms.admin.permission.application.service;

import com.wanted.codebombalms.admin.permission.application.command.ToggleAdminPermissionCommand;
import com.wanted.codebombalms.admin.permission.application.query.AdminAccountPageResult;
import com.wanted.codebombalms.admin.permission.application.query.AdminAccountPermissionResult;
import com.wanted.codebombalms.admin.permission.application.query.AdminAccountQueryRepository;
import com.wanted.codebombalms.admin.permission.application.query.AdminPermissionStates;
import com.wanted.codebombalms.admin.permission.application.query.UpdatedAdminPermission;
import com.wanted.codebombalms.admin.permission.application.usecase.GetAdminAccountsUseCase;
import com.wanted.codebombalms.admin.permission.application.usecase.ToggleAdminPermissionUseCase;
import com.wanted.codebombalms.admin.permission.domain.exception.AdminAuthErrorCode;
import com.wanted.codebombalms.admin.permission.domain.model.AdminPermissionType;
import com.wanted.codebombalms.admin.permission.domain.repository.AdminPermissionRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// master 전용 admin 계정 목록 조회와 권한 부여/회수 정책을 처리한다.
@Service
@RequiredArgsConstructor
public class AdminPermissionManagementService implements GetAdminAccountsUseCase, ToggleAdminPermissionUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final AdminPermissionRepository adminPermissionRepository;
    private final AdminAccountQueryRepository adminAccountQueryRepository;

    // 검색어와 페이지 조건을 검증한 뒤 admin 계정 목록을 조회한다.
    @Override
    @Transactional(readOnly = true)
    public AdminAccountPageResult getAdminAccounts(String keyword, int page, int size) {
        validatePageRequest(page, size);
        return adminAccountQueryRepository.findAdminAccounts(keyword, page, size);
    }

    // 대상 admin 계정의 권한 row를 생성하거나 삭제한 뒤 현재 권한 상태를 반환한다.
    @Override
    @Transactional
    public AdminAccountPermissionResult togglePermission(ToggleAdminPermissionCommand command) {
        validateCommand(command);

        User admin = findTargetAdmin(command.adminUserId());

        if (command.granted()) {
            adminPermissionRepository.grant(
                    command.adminUserId(),
                    command.permissionType(),
                    command.grantedBy()
            );
        } else {
            adminPermissionRepository.revoke(
                    command.adminUserId(),
                    command.permissionType()
            );
        }

        AdminPermissionStates permissionStates = getPermissionStates(command.adminUserId());

        return new AdminAccountPermissionResult(
                admin.getUserId(),
                admin.getEmail(),
                admin.getName(),
                admin.getNickname(),
                admin.getRole(),
                permissionStates,
                new UpdatedAdminPermission(command.permissionType(), command.granted())
        );
    }

    // 페이지 요청 값이 JPA PageRequest에 들어갈 수 있는 범위인지 확인한다.
    private void validatePageRequest(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ValidationException(AdminAuthErrorCode.INVALID_ADMIN_PERMISSION_REQUEST);
        }
    }

    // 권한 변경 command에 필수 값이 모두 들어있는지 확인한다.
    private void validateCommand(ToggleAdminPermissionCommand command) {
        if (command == null
                || command.adminUserId() == null
                || command.permissionType() == null
                || command.grantedBy() == null) {
            throw new ValidationException(AdminAuthErrorCode.INVALID_ADMIN_PERMISSION_REQUEST);
        }
    }

    // 권한 변경 대상이 삭제되지 않은 ADMIN 계정인지 확인한다.
    private User findTargetAdmin(Long adminUserId) {
        User user = userRepository.findByUserId(adminUserId)
                .orElseThrow(() -> new NotFoundException(AdminAuthErrorCode.ADMIN_ACCOUNT_NOT_FOUND));

        if (user.isDeleted()) {
            throw new NotFoundException(AdminAuthErrorCode.ADMIN_ACCOUNT_NOT_FOUND);
        }

        if (user.getRole() != UserRole.ADMIN) {
            throw new ConflictException(AdminAuthErrorCode.INVALID_ADMIN_PERMISSION_STATE);
        }

        return user;
    }

    // admin_permissions row 목록을 프론트가 쓰기 쉬운 boolean 상태로 변환한다.
    private AdminPermissionStates getPermissionStates(Long adminUserId) {
        List<AdminPermissionType> permissionTypes =
                adminPermissionRepository.findPermissionTypesByAdminUserId(adminUserId);

        return new AdminPermissionStates(
                permissionTypes.contains(AdminPermissionType.USER_MANAGEMENT),
                permissionTypes.contains(AdminPermissionType.RULE_MANAGEMENT)
        );
    }
}
