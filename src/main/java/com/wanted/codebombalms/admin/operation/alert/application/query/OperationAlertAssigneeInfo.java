package com.wanted.codebombalms.admin.operation.alert.application.query;

// 운영 알림 대상의 담당자 사용자 정보를 담는다.
public record OperationAlertAssigneeInfo(
        Long userId,
        String name,
        String nickname,
        String email,
        String role
) {
}
