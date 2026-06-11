package com.wanted.codebombalms.admin.permission.infrastructure.web;

import com.wanted.codebombalms.admin.permission.application.service.AdminPermissionCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AdminPermissionWebConfig implements WebMvcConfigurer {

    private final ObjectProvider<AdminPermissionCheckService> adminPermissionCheckServiceProvider;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        AdminPermissionCheckService adminPermissionCheckService =
                adminPermissionCheckServiceProvider.getIfAvailable();

        if (adminPermissionCheckService == null) {
            return;
        }

        registry.addInterceptor(new AdminPermissionInterceptor(adminPermissionCheckService))
                .addPathPatterns(
                        "/api/v1/users",
                        "/api/v1/users/*/enrollments",
                        "/api/v1/users/*/lock",
                        "/api/v1/admin/users/*",
                        "/api/v1/admin/students/*/problems",
                        "/api/v1/admin/operation-alerts/**",
                        "/api/v1/admin/automation-rules/**"
                );
    }
}
