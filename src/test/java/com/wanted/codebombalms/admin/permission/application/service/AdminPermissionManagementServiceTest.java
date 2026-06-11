package com.wanted.codebombalms.admin.permission.application.service;

import com.wanted.codebombalms.admin.permission.application.command.ToggleAdminPermissionCommand;
import com.wanted.codebombalms.admin.permission.application.query.AdminAccountPermissionResult;
import com.wanted.codebombalms.admin.permission.application.query.AdminAccountQueryRepository;
import com.wanted.codebombalms.admin.permission.domain.exception.AdminAuthErrorCode;
import com.wanted.codebombalms.admin.permission.domain.model.AdminPermissionType;
import com.wanted.codebombalms.admin.permission.domain.repository.AdminPermissionRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.user.domain.model.AuthProvider;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPermissionManagementService 단위 테스트")
class AdminPermissionManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminPermissionRepository adminPermissionRepository;

    @Mock
    private AdminAccountQueryRepository adminAccountQueryRepository;

    @InjectMocks
    private AdminPermissionManagementService adminPermissionManagementService;

    @Test
    @DisplayName("MASTER가 ADMIN에게 USER_MANAGEMENT 권한을 부여하면 row 생성을 요청하고 현재 상태를 반환한다.")
    void master_grants_user_management_permission() {
        // given
        given(userRepository.findByUserId(2L)).willReturn(Optional.of(createUser(2L, UserRole.ADMIN)));
        given(adminPermissionRepository.findPermissionTypesByAdminUserId(2L))
                .willReturn(List.of(AdminPermissionType.USER_MANAGEMENT));

        ToggleAdminPermissionCommand command = new ToggleAdminPermissionCommand(
                2L,
                AdminPermissionType.USER_MANAGEMENT,
                true,
                1L
        );

        // when
        AdminAccountPermissionResult result = adminPermissionManagementService.togglePermission(command);

        // then
        verify(adminPermissionRepository).grant(2L, AdminPermissionType.USER_MANAGEMENT, 1L);
        assertEquals(2L, result.userId());
        assertTrue(result.permissionStates().userManagement());
        assertFalse(result.permissionStates().ruleManagement());
        assertEquals(AdminPermissionType.USER_MANAGEMENT, result.updatedPermission().permissionType());
        assertTrue(result.updatedPermission().granted());
    }

    @Test
    @DisplayName("권한 변경 대상이 ADMIN이 아니면 409 예외를 던지고 권한 row를 수정하지 않는다.")
    void non_admin_target_throws_conflict() {
        // given
        given(userRepository.findByUserId(3L)).willReturn(Optional.of(createUser(3L, UserRole.STUDENT)));

        ToggleAdminPermissionCommand command = new ToggleAdminPermissionCommand(
                3L,
                AdminPermissionType.RULE_MANAGEMENT,
                true,
                1L
        );

        // when
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminPermissionManagementService.togglePermission(command)
        );

        // then
        assertEquals(AdminAuthErrorCode.INVALID_ADMIN_PERMISSION_STATE, exception.getErrorCode());
        verify(adminPermissionRepository, never()).grant(3L, AdminPermissionType.RULE_MANAGEMENT, 1L);
    }

    @Test
    @DisplayName("페이지 요청 값이 올바르지 않으면 ADM-AUTH-005 예외를 던진다.")
    void invalid_page_request_throws_validation_exception() {
        // when
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> adminPermissionManagementService.getAdminAccounts(null, -1, 20)
        );

        // then
        assertEquals(AdminAuthErrorCode.INVALID_ADMIN_PERMISSION_REQUEST, exception.getErrorCode());
    }

    // 테스트용 사용자 도메인 객체를 만든다.
    private User createUser(Long userId, UserRole role) {
        return User.restore(
                userId,
                role,
                "user" + userId + "@test.com",
                "ENCODED_PW",
                "테스트",
                "tester" + userId,
                "010-1234-5678",
                AuthProvider.LOCAL,
                null,
                true,
                false,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
    }
}
