package com.wanted.codebombalms.domain.admin.operation.alert.application.command;

import com.wanted.codebombalms.domain.admin.operation.alert.domain.model.OperationAlertStatus;

public record UpdateOperationAlertStatusCommand(
        Long operationAlertId,
        OperationAlertStatus status,
        String adminMemo,
        Long resolvedBy
) {
}