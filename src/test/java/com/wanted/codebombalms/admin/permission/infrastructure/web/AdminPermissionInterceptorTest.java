package com.wanted.codebombalms.admin.permission.infrastructure.web;

import com.wanted.codebombalms.admin.permission.application.service.AdminPermissionCheckService;
import com.wanted.codebombalms.admin.permission.domain.exception.AdminAuthErrorCode;
import com.wanted.codebombalms.admin.permission.domain.model.AdminPermissionType;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPermissionInterceptor 단위 테스트")
class AdminPermissionInterceptorTest {

    @Mock
    private AdminPermissionCheckService adminPermissionCheckService;

    private AdminPermissionInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new AdminPermissionInterceptor(adminPermissionCheckService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("MASTER는 permission 조회 없이 대상 admin API를 통과하고 ROLE_ADMIN을 보강한다.")
    void master_passes_without_permission_check() {
        // given
        authenticate(1L, "ROLE_MASTER");
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/api/v1/admin/operation-alerts"
        );

        // when
        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        // then
        assertTrue(result);
        assertTrue(hasAuthority("ROLE_MASTER"));
        assertTrue(hasAuthority("ROLE_ADMIN"));
        verifyNoInteractions(adminPermissionCheckService);
    }

    @Test
    @DisplayName("ADMIN이 유저 관리 API를 호출하면 USER_MANAGEMENT 권한을 검증한다.")
    void admin_user_management_api_requires_user_management_permission() {
        // given
        authenticate(2L, "ROLE_ADMIN");
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/api/v1/users/10/enrollments"
        );

        // when
        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        // then
        assertTrue(result);
        verify(adminPermissionCheckService).requirePermission(
                2L,
                AdminPermissionType.USER_MANAGEMENT
        );
    }

    @Test
    @DisplayName("ADMIN이 운영 규칙 API를 호출하면 RULE_MANAGEMENT 권한을 검증한다.")
    void admin_rule_management_api_requires_rule_management_permission() {
        // given
        authenticate(2L, "ROLE_ADMIN");
        MockHttpServletRequest request = new MockHttpServletRequest(
                "PATCH",
                "/api/v1/admin/automation-rules/1/enabled"
        );

        // when
        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        // then
        assertTrue(result);
        verify(adminPermissionCheckService).requirePermission(
                2L,
                AdminPermissionType.RULE_MANAGEMENT
        );
    }

    @Test
    @DisplayName("USER_MANAGEMENT만 있는 ADMIN이 운영 규칙 API에 접근하면 MASTER 문의 메시지와 함께 403 예외를 반환한다.")
    void user_management_admin_cannot_access_rule_management_api() {
        // given
        authenticate(2L, "ROLE_ADMIN");
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/api/v1/admin/automation-rules"
        );

        doThrow(new ForbiddenException(AdminAuthErrorCode.RULE_MANAGEMENT_PERMISSION_REQUIRED))
                .when(adminPermissionCheckService)
                .requirePermission(2L, AdminPermissionType.RULE_MANAGEMENT);

        // when
        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object())
        );

        // then
        assertEquals(AdminAuthErrorCode.RULE_MANAGEMENT_PERMISSION_REQUIRED, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("MASTER 관리자에게 권한 부여를 요청해주세요."));
    }

    private void authenticate(Long userId, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                )
        );
    }

    private boolean hasAuthority(String authority) {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }
}
