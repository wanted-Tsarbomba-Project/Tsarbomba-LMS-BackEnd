package com.wanted.codebombalms.admin.operation.alert.application.command;

// 운영 알림 관리자 메모 수정 요청 값을 담는다.
public record UpdateOperationAlertMemoCommand(
        Long operationAlertId,
        String adminMemo
) {
}
