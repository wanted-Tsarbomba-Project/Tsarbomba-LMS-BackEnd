package com.wanted.codebombalms.admin.permission.infrastructure.web;

import com.wanted.codebombalms.admin.permission.application.service.AdminPermissionCheckService;
import com.wanted.codebombalms.admin.permission.domain.exception.AdminAuthErrorCode;
import com.wanted.codebombalms.admin.permission.domain.model.AdminPermissionType;
import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class AdminPermissionInterceptor implements HandlerInterceptor {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_MASTER = "ROLE_MASTER";

    private static final List<AdminPermissionRule> PERMISSION_RULES = List.of(
            new AdminPermissionRule("GET", "^/api/v1/users$", AdminPermissionType.USER_MANAGEMENT),
            new AdminPermissionRule("GET", "^/api/v1/admin/users/[0-9]+$", AdminPermissionType.USER_MANAGEMENT),
            new AdminPermissionRule("GET", "^/api/v1/users/[0-9]+/enrollments$", AdminPermissionType.USER_MANAGEMENT),
            new AdminPermissionRule("GET", "^/api/v1/admin/students/[0-9]+/problems$", AdminPermissionType.USER_MANAGEMENT),
            new AdminPermissionRule("PATCH", "^/api/v1/users/[0-9]+/lock$", AdminPermissionType.USER_MANAGEMENT),
            new AdminPermissionRule("GET", "^/api/v1/admin/operation-alerts$", AdminPermissionType.RULE_MANAGEMENT),
            new AdminPermissionRule("GET", "^/api/v1/admin/operation-alerts/[0-9]+$", AdminPermissionType.RULE_MANAGEMENT),
            new AdminPermissionRule("PATCH", "^/api/v1/admin/operation-alerts/[0-9]+/memo$", AdminPermissionType.RULE_MANAGEMENT),
            new AdminPermissionRule("PATCH", "^/api/v1/admin/operation-alerts/[0-9]+/status$", AdminPermissionType.RULE_MANAGEMENT),
            new AdminPermissionRule("DELETE", "^/api/v1/admin/operation-alerts/[0-9]+$", AdminPermissionType.RULE_MANAGEMENT),
            new AdminPermissionRule("GET", "^/api/v1/admin/automation-rules$", AdminPermissionType.RULE_MANAGEMENT),
            new AdminPermissionRule("PATCH", "^/api/v1/admin/automation-rules$", AdminPermissionType.RULE_MANAGEMENT),
            new AdminPermissionRule("PATCH", "^/api/v1/admin/automation-rules/[0-9]+/enabled$", AdminPermissionType.RULE_MANAGEMENT)
    );

    private final AdminPermissionCheckService adminPermissionCheckService;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        Optional<AdminPermissionType> requiredPermission = findRequiredPermission(request);
        if (requiredPermission.isEmpty()) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(authentication)) {
            return true;
        }

        if (hasRole(authentication, ROLE_MASTER)) {
            appendAdminRoleForMaster(authentication);
            return true;
        }

        if (!hasRole(authentication, ROLE_ADMIN)) {
            return true;
        }

        Long adminUserId = extractUserId(authentication.getPrincipal());
        if (adminUserId == null) {
            throw new ForbiddenException(AdminAuthErrorCode.ADMIN_PERMISSION_REQUIRED);
        }

        adminPermissionCheckService.requirePermission(
                adminUserId,
                requiredPermission.get()
        );
        return true;
    }

    private Optional<AdminPermissionType> findRequiredPermission(HttpServletRequest request) {
        String method = request.getMethod();
        String path = getPath(request);

        return PERMISSION_RULES.stream()
                .filter(rule -> rule.matches(method, path))
                .map(AdminPermissionRule::permissionType)
                .findFirst();
    }

    private String getPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();

        if (contextPath == null || contextPath.isBlank()) {
            return requestUri;
        }
        return requestUri.substring(contextPath.length());
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }

    private void appendAdminRoleForMaster(Authentication authentication) {
        if (hasRole(authentication, ROLE_ADMIN)) {
            return;
        }

        Collection<? extends GrantedAuthority> currentAuthorities = authentication.getAuthorities();
        List<GrantedAuthority> updatedAuthorities = new ArrayList<>(currentAuthorities);
        updatedAuthorities.add(new SimpleGrantedAuthority(ROLE_ADMIN));

        UsernamePasswordAuthenticationToken updatedAuthentication =
                new UsernamePasswordAuthenticationToken(
                        authentication.getPrincipal(),
                        authentication.getCredentials(),
                        updatedAuthorities
                );
        updatedAuthentication.setDetails(authentication.getDetails());
        SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);
    }

    private Long extractUserId(Object principal) {
        if (principal instanceof Long userId) {
            return userId;
        }
        if (principal instanceof String userId) {
            try {
                return Long.valueOf(userId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private record AdminPermissionRule(
            String method,
            Pattern pathPattern,
            AdminPermissionType permissionType
    ) {
        private AdminPermissionRule(
                String method,
                String pathRegex,
                AdminPermissionType permissionType
        ) {
            this(method, Pattern.compile(pathRegex), permissionType);
        }

        private boolean matches(String requestMethod, String requestPath) {
            return method.equalsIgnoreCase(requestMethod)
                    && pathPattern.matcher(requestPath).matches();
        }
    }
}
