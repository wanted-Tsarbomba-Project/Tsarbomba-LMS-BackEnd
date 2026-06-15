package com.wanted.codebombalms.admin.permission.application.query;

// admin 계정이 보유한 세부 권한 상태를 boolean으로 펼쳐 담는다.
public record AdminPermissionStates(
        boolean userManagement,
        boolean ruleManagement
) {
}
