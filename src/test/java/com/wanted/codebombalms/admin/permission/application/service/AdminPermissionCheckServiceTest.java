package com.wanted.codebombalms.admin.permission.application.service;

import com.wanted.codebombalms.admin.permission.domain.exception.AdminAuthErrorCode;
import com.wanted.codebombalms.admin.permission.domain.model.AdminPermissionType;
import com.wanted.codebombalms.admin.permission.domain.repository.AdminPermissionRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPermissionCheckService 단위 테스트")
class AdminPermissionCheckServiceTest {

    @Mock
    private AdminPermissionRepository adminPermissionRepository;

    @InjectMocks
    private AdminPermissionCheckService adminPermissionCheckService;

    @Test
    @DisplayName("ADMIN이 필요한 권한을 가지고 있으면 통과한다.")
    void admin_with_required_permission_passes() {
        // given
        given(adminPermissionRepository.existsByAdminUserIdAndPermissionType(
                2L,
                AdminPermissionType.USER_MANAGEMENT
        )).willReturn(true);

        // when & then
        assertDoesNotThrow(() -> adminPermissionCheckService.requirePermission(
                2L,
                AdminPermissionType.USER_MANAGEMENT
        ));
    }

    @Test
    @DisplayName("ADMIN이 필요한 권한을 가지고 있지 않으면 권한별 403 예외를 던진다.")
    void admin_without_required_permission_throws_forbidden() {
        // given
        given(adminPermissionRepository.existsByAdminUserIdAndPermissionType(
                2L,
                AdminPermissionType.RULE_MANAGEMENT
        )).willReturn(false);

        // when
        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> adminPermissionCheckService.requirePermission(
                        2L,
                        AdminPermissionType.RULE_MANAGEMENT
                )
        );

        // then
        assertEquals(403, exception.getHttpStatus());
        assertEquals(AdminAuthErrorCode.RULE_MANAGEMENT_PERMISSION_REQUIRED, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("MASTER 관리자에게 권한 부여를 요청해주세요."));
    }
}
