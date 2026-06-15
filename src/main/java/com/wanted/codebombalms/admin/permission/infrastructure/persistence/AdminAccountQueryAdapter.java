package com.wanted.codebombalms.admin.permission.infrastructure.persistence;

import com.wanted.codebombalms.admin.permission.application.query.AdminAccountPageResult;
import com.wanted.codebombalms.admin.permission.application.query.AdminAccountQueryRepository;
import com.wanted.codebombalms.admin.permission.application.query.AdminAccountSummary;
import com.wanted.codebombalms.admin.permission.application.query.AdminPermissionStates;
import com.wanted.codebombalms.admin.permission.domain.model.AdminPermissionType;
import com.wanted.codebombalms.user.domain.model.UserRole;
import com.wanted.codebombalms.user.infrastructure.persistence.SpringDataUserRepository;
import com.wanted.codebombalms.user.infrastructure.persistence.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// users와 admin_permissions를 조합해 master 화면용 admin 목록을 조회한다.
@Repository
@RequiredArgsConstructor
public class AdminAccountQueryAdapter implements AdminAccountQueryRepository {

    private final SpringDataUserRepository userRepository;
    private final SpringDataAdminPermissionRepository permissionRepository;

    // ADMIN role 계정만 조회하고 권한 row 존재 여부를 boolean 상태로 매핑한다.
    @Override
    public AdminAccountPageResult findAdminAccounts(String keyword, int page, int size) {
        Page<UserJpaEntity> adminAccounts = userRepository.findActiveByRoleAndKeyword(
                UserRole.ADMIN,
                normalizeKeyword(keyword),
                PageRequest.of(page, size)
        );

        Map<Long, Set<AdminPermissionType>> permissionMap = findPermissionMap(
                adminAccounts.getContent()
        );

        List<AdminAccountSummary> items = adminAccounts.getContent().stream()
                .map(admin -> toSummary(admin, permissionMap.getOrDefault(
                        admin.getUserId(),
                        EnumSet.noneOf(AdminPermissionType.class)
                )))
                .toList();

        return new AdminAccountPageResult(
                items,
                page,
                size,
                adminAccounts.getTotalElements(),
                adminAccounts.getTotalPages()
        );
    }

    // 빈 검색어는 null로 바꿔 JPQL 조건에서 전체 조회로 처리되게 한다.
    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    // 조회된 admin 계정들의 권한 row를 adminUserId 기준으로 묶는다.
    private Map<Long, Set<AdminPermissionType>> findPermissionMap(List<UserJpaEntity> adminAccounts) {
        List<Long> adminUserIds = adminAccounts.stream()
                .map(UserJpaEntity::getUserId)
                .toList();

        if (adminUserIds.isEmpty()) {
            return Map.of();
        }

        return permissionRepository.findAllByAdminUserIdIn(adminUserIds)
                .stream()
                .collect(Collectors.groupingBy(
                        AdminPermissionJpaEntity::getAdminUserId,
                        Collectors.mapping(
                                AdminPermissionJpaEntity::getPermissionType,
                                Collectors.toCollection(() -> EnumSet.noneOf(AdminPermissionType.class))
                        )
                ));
    }

    // user 엔티티와 권한 집합을 admin 목록 한 행으로 변환한다.
    private AdminAccountSummary toSummary(
            UserJpaEntity admin,
            Set<AdminPermissionType> permissionTypes
    ) {
        return new AdminAccountSummary(
                admin.getUserId(),
                admin.getEmail(),
                admin.getName(),
                admin.getNickname(),
                admin.getRole(),
                admin.isLocked(),
                new AdminPermissionStates(
                        permissionTypes.contains(AdminPermissionType.USER_MANAGEMENT),
                        permissionTypes.contains(AdminPermissionType.RULE_MANAGEMENT)
                ),
                admin.getCreatedAt()
        );
    }
}
