package com.wanted.codebombalms.admin.permission.presentation.api.response;

import com.wanted.codebombalms.admin.permission.application.query.AdminPermissionStates;

// 프론트의 user/rule 권한 칼럼에 바로 사용할 boolean 상태를 담는다.
public record AdminPermissionStatesResponse(
        boolean userManagement,
        boolean ruleManagement
) {

    // application 권한 상태를 API response로 변환한다.
    public static AdminPermissionStatesResponse from(AdminPermissionStates states) {
        return new AdminPermissionStatesResponse(
                states.userManagement(),
                states.ruleManagement()
        );
    }
}
