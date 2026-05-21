package com.wanted.codebombalms.admin.operation.alert.application.command;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;

public record UpdateOperationAlertStatusCommand(
        Long operationAlertId,
        OperationAlertStatus status,
        Long resolvedBy
) {
}
