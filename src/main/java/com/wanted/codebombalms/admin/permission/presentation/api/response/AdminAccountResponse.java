package com.wanted.codebombalms.admin.permission.presentation.api.response;

import com.wanted.codebombalms.admin.permission.application.query.AdminAccountSummary;

import java.time.LocalDateTime;

// admin 목록 테이블 한 행에 표시할 계정 정보와 권한 상태를 담는다.
public record AdminAccountResponse(
        Long userId,
        String email,
        String name,
        String nickname,
        String role,
        boolean locked,
        AdminPermissionStatesResponse permissionStates,
        LocalDateTime createdAt
) {

    // application summary를 API response item으로 변환한다.
    public static AdminAccountResponse from(AdminAccountSummary summary) {
        return new AdminAccountResponse(
                summary.userId(),
                summary.email(),
                summary.name(),
                summary.nickname(),
                summary.role().name(),
                summary.locked(),
                AdminPermissionStatesResponse.from(summary.permissionStates()),
                summary.createdAt()
        );
    }
}
